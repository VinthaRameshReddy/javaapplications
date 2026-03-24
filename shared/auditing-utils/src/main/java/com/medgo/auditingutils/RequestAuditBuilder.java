package com.medgo.auditingutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * Builds request JSON for audit logging using a StringBuffer.
 * Handles MultipartFile (single, array, collection), String, and other DTO arguments.
 *
 * <p><b>Why MultipartFile?</b> The aspect runs around all {@code @PostMapping} controller methods.
 * Some endpoints accept file uploads (e.g. reimbursement document upload, file upload API). Their
 * method arguments include {@link org.springframework.web.multipart.MultipartFile} or
 * {@code List<MultipartFile>}. We cannot serialize a MultipartFile with Jackson (it is not a
 * POJO and contains file bytes). So we handle it explicitly and log only metadata:
 * {@code fileName} and {@code fileSize} — never the file content.
 */
public final class RequestAuditBuilder {

    private RequestAuditBuilder() {
    }

    /**
     * Builds a JSON array string from controller method arguments for audit logging.
     * Uses StringBuffer for thread-safe string building; serializes MultipartFile as
     * { "fileName": "...", "fileSize": N } and other types via ObjectMapper.
     *
     * @param args         controller method arguments
     * @param objectMapper mapper for non-file, non-string arguments
     * @return JSON array string, e.g. [{"fileName":"a.txt","fileSize":1024},"string",{...}]
     */
    public static String buildRequestJson(Object[] args, ObjectMapper objectMapper) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuffer buffer = new StringBuffer(256);
        buffer.append("[");
        for (Object arg : args) {
            try {
                appendArg(buffer, arg, objectMapper);
                buffer.append(",");
            } catch (Exception ignored) {
                // skip failed serialization; comma may leave trailing, trimmed below
            }
        }
        if (buffer.charAt(buffer.length() - 1) == ',') {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Appends one argument to the buffer: MultipartFile (single/array/collection), String, or JSON-serialized object.
     */
    private static void appendArg(StringBuffer buffer, Object arg, ObjectMapper objectMapper) throws Exception {
        if (arg instanceof MultipartFile file) {
            appendMultipartFile(buffer, file);
        } else if (arg instanceof MultipartFile[] files) {
            buffer.append("[");
            for (int i = 0; i < files.length; i++) {
                if (i > 0) buffer.append(",");
                appendMultipartFile(buffer, files[i]);
            }
            buffer.append("]");
        } else if (arg instanceof Collection<?> coll && isMultipartFileCollection(coll)) {
            buffer.append("[");
            int idx = 0;
            for (Object item : coll) {
                if (item instanceof MultipartFile f) {
                    if (idx++ > 0) buffer.append(",");
                    appendMultipartFile(buffer, f);
                }
            }
            buffer.append("]");
        } else if (arg instanceof String s) {
            buffer.append("\"").append(escapeJsonString(s)).append("\"");
        } else {
            buffer.append(objectMapper.writeValueAsString(arg));
        }
    }

    private static void appendMultipartFile(StringBuffer buffer, MultipartFile file) {
        String fileName = nullToEmpty(file.getOriginalFilename());
        buffer.append("{")
                .append("\"fileName\":\"").append(escapeJsonString(fileName)).append("\",")
                .append("\"fileSize\":").append(file.getSize())
                .append("}");
    }

    /**
     * Returns true if the argument is a file type (MultipartFile, MultipartFile[], or collection of MultipartFile).
     * Use this to skip file args when extracting audit fields from request DTOs.
     */
    public static boolean isFileOrFileCollection(Object arg) {
        if (arg == null) return false;
        if (arg instanceof MultipartFile) return true;
        if (arg instanceof MultipartFile[]) return true;
        return arg instanceof Collection<?> c && isMultipartFileCollection(c);
    }

    private static boolean isMultipartFileCollection(Collection<?> coll) {
        if (coll == null || coll.isEmpty()) return true;
        for (Object o : coll) {
            if (o != null && !(o instanceof MultipartFile)) return false;
        }
        return true;
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        StringBuffer sb = new StringBuffer(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
