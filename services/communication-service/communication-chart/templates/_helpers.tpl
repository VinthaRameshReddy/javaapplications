{{- define "communication-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "communication-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
