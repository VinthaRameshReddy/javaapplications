{{- define "shared-reimbursement-chart.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "shared-reimbursement-chart.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

