{{- define "reimbursement-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "reimbursement-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

