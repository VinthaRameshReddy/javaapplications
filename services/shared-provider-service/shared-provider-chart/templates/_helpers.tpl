{{- define "shared-provider-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "shared-provider-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
