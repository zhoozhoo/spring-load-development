{{/*
Expand the name of the chart.
*/}}
{{- define "spring-load-development.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "spring-load-development.fullname" -}}
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
{{- define "spring-load-development.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "spring-load-development.labels" -}}
helm.sh/chart: {{ include "spring-load-development.chart" . }}
{{ include "spring-load-development.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "spring-load-development.selectorLabels" -}}
app.kubernetes.io/name: {{ include "spring-load-development.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "spring-load-development.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "spring-load-development.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create service DNS name for a microservices
*/}}
{{- define "spring-load-development.serviceDNS" -}}
{{- $serviceName := .serviceName }}
{{- $context := .context }}
{{- printf "%s.%s.svc.cluster.local" $serviceName $context.Values.microservices.namespace }}
{{- end }}

{{/*
Common labels for a specific microservice component
*/}}
{{- define "spring-load-development.microservice.labels" -}}
{{- $componentName := .componentName }}
{{- $context := .context }}
{{ include "spring-load-development.labels" $context }}
app.kubernetes.io/component: {{ $componentName }}
{{- end }}

{{/*
Selector labels for a specific microservice component
*/}}
{{- define "spring-load-development.microservice.selectorLabels" -}}
{{- $componentName := .componentName }}
{{- $context := .context }}
{{ include "spring-load-development.selectorLabels" $context }}
app.kubernetes.io/component: {{ $componentName }}
{{- end }}

{{/*
Generate environment variables common to all microservices
*/}}
{{- define "spring-load-development.microservice.commonEnv" -}}
{{- $componentName := .componentName }}
{{- $context := .context }}
- name: SPRING_PROFILES_ACTIVE
  value: "kubernetes"
- name: SPRING_APPLICATION_NAME
  value: {{ $componentName | quote }}
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: {{ $context.Values.otelCollector.grpcEndpoint | default "http://otel-collector-service.observability.svc.cluster.local:4317" }}
- name: OTEL_EXPORTER_OTLP_PROTOCOL
  value: "grpc"
- name: KEYCLOAK_BASE_URL
  value: {{ $context.Values.keycloak.integration.baseUrl | default "http://keycloak-service.keycloak.svc.cluster.local:8080" }}
{{- end }}

{{/*
Generate database environment variables for services that need database access
*/}}
{{- define "spring-load-development.microservice.databaseEnv" -}}
{{- $context := . }}
- name: SPRING_R2DBC_URL
  value: "r2dbc:postgresql://postgres-service.{{ $context.Values.postgresql.namespace | default "default" }}.svc.cluster.local:5432/loadsdb"
- name: SPRING_R2DBC_USERNAME
  value: {{ $context.Values.postgresql.auth.username | default "user" }}
- name: SPRING_R2DBC_PASSWORD
  value: {{ $context.Values.postgresql.auth.password | default "password" }}
{{- end }}

{{/*
Generate microservice deployment
*/}}
{{- define "spring-load-development.microservice.deployment" -}}
{{- $componentName := .componentName }}
{{- $config := .config }}
{{- $context := .context }}
{{- $needsDatabase := .needsDatabase | default false }}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "spring-load-development.fullname" $context }}-{{ $componentName }}
  namespace: {{ $config.namespace | default $context.Values.microservices.namespace }}
  labels:
    {{- include "spring-load-development.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
spec:
  replicas: {{ $config.replicas | default 1 }}
  selector:
    matchLabels:
      {{- include "spring-load-development.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "spring-load-development.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 8 }}
    spec:
      serviceAccountName: {{ include "spring-load-development.serviceAccountName" $context }}
      securityContext:
        {{- include "spring-load-development.podSecurityContext" $context | nindent 8 }}
      containers:
      - name: {{ $componentName }}
        image: {{ $config.image.repository }}:{{ $config.image.tag | default $context.Chart.AppVersion }}
        imagePullPolicy: {{ $config.image.pullPolicy | default "IfNotPresent" }}
        securityContext:
          {{- include "spring-load-development.containerSecurityContext" $context | nindent 10 }}
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        {{- include "spring-load-development.microservice.commonEnv" (dict "componentName" $componentName "context" $context) | nindent 8 }}
        {{- if $needsDatabase }}
        {{- include "spring-load-development.microservice.databaseEnv" $context | nindent 8 }}
        {{- end }}
        {{- if $config.env }}
        {{- toYaml $config.env | nindent 8 }}
        {{- end }}
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: http
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: http
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: log4j2-volume
          mountPath: /etc/config
          readOnly: true
        resources:
          {{- toYaml $config.resources | nindent 10 }}
      volumes:
      - name: log4j2-volume
        configMap:
          name: log4j2-config
      {{- with $config.nodeSelector }}
      nodeSelector:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with $config.affinity }}
      affinity:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with $config.tolerations }}
      tolerations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
{{- end }}

{{/*
Generate microservice service
*/}}
{{- define "spring-load-development.microservice.service" -}}
{{- $componentName := .componentName }}
{{- $serviceName := .serviceName }}
{{- $config := .config }}
{{- $context := .context }}
---
apiVersion: v1
kind: Service
metadata:
  name: {{ $serviceName }}
  namespace: {{ $config.namespace | default $context.Values.microservices.namespace }}
  labels:
    {{- include "spring-load-development.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
spec:
  type: {{ $config.service.type | default "ClusterIP" }}
  ports:
  - port: {{ $config.service.port | default 8080 }}
    targetPort: http
    protocol: TCP
    name: http
    {{- if and (eq ($config.service.type | default "ClusterIP") "NodePort") $config.service.nodePort }}
    nodePort: {{ $config.service.nodePort }}
    {{- end }}
  selector:
    {{- include "spring-load-development.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
{{- end }}

{{/*
Generate microservice ingress
*/}}
{{- define "spring-load-development.microservice.ingress" -}}
{{- $componentName := .componentName }}
{{- $serviceName := .serviceName | default (printf "%s-%s" (include "spring-load-development.fullname" .context) $componentName) }}
{{- $config := .config }}
{{- $context := .context }}
{{- if $config.ingress.enabled }}
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "spring-load-development.fullname" $context }}-{{ $componentName }}
  namespace: {{ $config.namespace | default $context.Values.microservices.namespace }}
  labels:
    {{- include "spring-load-development.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
  {{- with $config.ingress.annotations }}
  annotations:
    {{- toYaml . | nindent 4 }}
  {{- end }}
spec:
  {{- if $config.ingress.tls }}
  tls:
    {{- range $config.ingress.tls }}
    - hosts:
        {{- range .hosts }}
        - {{ . | quote }}
        {{- end }}
      secretName: {{ .secretName }}
    {{- end }}
  {{- end }}
  rules:
    {{- range $config.ingress.hosts }}
    - host: {{ .host | quote }}
      http:
        paths:
          {{- range .paths }}
          - path: {{ .path }}
            pathType: {{ .pathType }}
            backend:
              service:
                name: {{ $serviceName }}
                port:
                  number: {{ $config.service.port | default 8080 }}
          {{- end }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
Pod security context
*/}}
{{- define "spring-load-development.podSecurityContext" -}}
runAsNonRoot: true
runAsUser: {{ .Values.securityContext.runAsUser | default 1000 }}
fsGroup: {{ .Values.securityContext.fsGroup | default 2000 }}
seccompProfile:
  type: RuntimeDefault
{{- end }}

{{/*
Container security context
*/}}
{{- define "spring-load-development.containerSecurityContext" -}}
allowPrivilegeEscalation: false
runAsNonRoot: true
runAsUser: {{ .Values.securityContext.runAsUser | default 1000 }}
capabilities:
  drop:
  - ALL
readOnlyRootFilesystem: false
seccompProfile:
  type: RuntimeDefault
{{- end }}

{{/*
Container security context for databases (needs write access)
*/}}
{{- define "spring-load-development.databaseSecurityContext" -}}
allowPrivilegeEscalation: false
runAsNonRoot: true
runAsUser: 999
capabilities:
  drop:
  - ALL
readOnlyRootFilesystem: false
seccompProfile:
  type: RuntimeDefault
{{- end }}

