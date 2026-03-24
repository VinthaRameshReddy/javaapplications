{{- define "loa-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "loa-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
