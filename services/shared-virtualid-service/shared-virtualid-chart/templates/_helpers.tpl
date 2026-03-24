{{- define "shared-virtualid-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "shared-virtualid-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

