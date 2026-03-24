package com.medgo.filemanagement.utils;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AzureUtil {
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String connectionString;

    public BlobContainerClient blobServiceClientBuilder() {
        return new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
    }

    public String generateSas(String blobName) {
        var blobClient = blobServiceClientBuilder().getBlobClient(blobName);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        OffsetDateTime expiry = OffsetDateTime.now().plusDays(365);
        var signatures = new BlobServiceSasSignatureValues(expiry, permission)
                .setStartTime(OffsetDateTime.now());
        return blobClient.generateSas(signatures);
    }

    public List<BlobItem> getBlobsByPrefix(String prefix, Integer size) {
        BlobContainerClient container = blobServiceClientBuilder();
        ListBlobsOptions options = new ListBlobsOptions();
        options.setMaxResultsPerPage(size);
        options.setPrefix(prefix);

        BlobListDetails details = new BlobListDetails();
        details.setRetrieveMetadata(true);
        details.setRetrieveTags(true);
        options.setDetails(details);

        var items = container.listBlobs(options, Duration.ofSeconds(60));
        var iterator = items.iterableByPage().iterator();
        ArrayList<BlobItem> blobs = new ArrayList<>();

        while (iterator.hasNext()) {
            var pageResponse = iterator.next();
            var blobsPage = pageResponse.getValue();
            blobs.addAll(blobsPage);
            if (pageResponse.getContinuationToken() == null) break;
        }
        return blobs;
    }

    public static String getPathByTags(Map<String, String> tags) {
        var path = "";
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        if (tags.containsKey("databaseName"))
            path = tags.get("databaseName") + "/";

        if (tags.containsKey("tableName")) {
            path += tags.get("tableName") + "/";
        }

        if (tags.containsKey("id")) {
            path += tags.get("id");
        }

        return path;
    }
}