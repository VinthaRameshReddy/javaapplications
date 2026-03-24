{{- define "auth-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "auth-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
