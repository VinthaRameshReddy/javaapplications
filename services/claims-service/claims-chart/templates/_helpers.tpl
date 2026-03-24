{{- define "claims-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "claims-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
