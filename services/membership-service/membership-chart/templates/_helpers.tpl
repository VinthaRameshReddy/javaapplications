{{- define "membership-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "membership-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

