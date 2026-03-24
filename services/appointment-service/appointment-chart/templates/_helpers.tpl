{{- define "appointment-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "appointment-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
