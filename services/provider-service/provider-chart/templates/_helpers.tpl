{{- define "provider-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "provider-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
