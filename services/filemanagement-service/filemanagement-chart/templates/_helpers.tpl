{{- define "filemanagement-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "filemanagement-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

