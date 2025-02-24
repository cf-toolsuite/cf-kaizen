{{/*
Expand the name of the chart.
*/}}
{{- define "uaa.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "uaa.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "uaa.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "uaa.labels" -}}
helm.sh/chart: {{ include "uaa.chart" . }}
{{ include "uaa.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/name: "uaa"
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: "authorization_server"
{{- end }}

{{/*
Selector labels
*/}}
{{- define "uaa.selectorLabels" -}}
app.kubernetes.io/name: {{ include "uaa.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "jwt.policy.keys" -}}
jwt:
  token:
    policy:
      activeKeyId: {{ .Values.jwt.policy.activeKeyId }}
      keys:
        signingAlg: {{ .Values.jwt.policy.keys.signingAlg | default "RS256" }}
        {{ .Values.jwt.policy.activeKeyId }}:
          signingKey: |-
            {{ index .Values.jwt.policy.keys .Values.jwt.policy.activeKeyId "signingKey" }}
{{- with index .Values.jwt.policy.keys .Values.jwt.policy.activeKeyId }}
{{- if .signingCert }}
          signingCert: |-
            {{ .signingCert }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "saml.keys" -}}
login:
  saml:
    serviceProviderKey: |-
      {{ .Values.login.saml.serviceProviderKey }}
    serviceProviderCertificate: |-
      {{ .Values.login.saml.serviceProviderCertificate }}
    activeKeyId: {{ .Values.login.saml.activeKeyId }}
    keys:
      {{ .Values.login.saml.activeKeyId }}:
        key: |-
          {{ index .Values.login.saml.keys .Values.login.saml.activeKeyId "key" }}
{{- with index .Values.login.saml.keys .Values.login.saml.activeKeyId }}
{{- if .certificate }}
        certificate: |-
          {{ .certificate }}
{{- end }}
{{- end }}
{{- end -}}