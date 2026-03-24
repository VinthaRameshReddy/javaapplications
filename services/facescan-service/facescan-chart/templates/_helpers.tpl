{{- define "facescan-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "facescan-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
