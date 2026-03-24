{{- define "shared-utilization-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "shared-utilization-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
