{{- define "shared-membership-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "shared-membership-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

