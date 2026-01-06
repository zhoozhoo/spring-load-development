{{/*
Expand the name of the chart.
*/}}
{{- define "reloading.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "reloading.fullname" -}}
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
{{- define "reloading.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "reloading.labels" -}}
helm.sh/chart: {{ include "reloading.chart" . }}
{{ include "reloading.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "reloading.selectorLabels" -}}
app.kubernetes.io/name: {{ include "reloading.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "reloading.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "reloading.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Common labels for a specific microservice component
*/}}
{{- define "reloading.microservice.labels" -}}
{{- $componentName := .componentName }}
{{- $context := .context }}
{{ include "reloading.labels" $context }}
app.kubernetes.io/component: {{ $componentName }}
{{- end }}

{{/*
Selector labels for a specific microservice component
*/}}
{{- define "reloading.microservice.selectorLabels" -}}
{{- $componentName := .componentName }}
{{- $context := .context }}
{{ include "reloading.selectorLabels" $context }}
app.kubernetes.io/component: {{ $componentName }}
{{- end }}

{{/*
Generate environment variables common to all microservices
*/}}
{{- define "reloading.microservice.commonEnv" -}}
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
  value: {{ $context.Values.keycloak.baseUrl | default "http://keycloak-service.keycloak.svc.cluster.local:8080" }}
{{- end }}

{{/*
Generate database environment variables for services that need database access
*/}}
{{- define "reloading.microservice.databaseEnv" -}}
{{- $context := . }}
- name: SPRING_R2DBC_URL
  value: "r2dbc:postgresql://postgres-service.{{ $context.Values.postgresql.namespace | default "postgres" }}.svc.cluster.local:5432/loadsdb"
- name: SPRING_R2DBC_USERNAME
  value: {{ $context.Values.postgresql.auth.username | default "user" }}
- name: SPRING_R2DBC_PASSWORD
  value: {{ $context.Values.postgresql.auth.password | default "password" }}
{{- end }}

{{/*
Generate microservice deployment
*/}}
{{- define "reloading.microservice.deployment" -}}
{{- $componentName := .componentName }}
{{- $config := .config }}
{{- $context := .context }}
{{- $needsDatabase := .needsDatabase | default false }}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "reloading.fullname" $context }}-{{ $componentName }}
  namespace: {{ $context.Values.namespace }}
  labels:
    {{- include "reloading.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
spec:
  replicas: {{ $config.replicas | default 1 }}
  selector:
    matchLabels:
      {{- include "reloading.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "reloading.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 8 }}
    spec:
      serviceAccountName: {{ include "reloading.serviceAccountName" $context }}
      securityContext:
        {{- include "reloading.podSecurityContext" $context | nindent 8 }}
      containers:
      - name: {{ $componentName }}
        image: {{ $config.image.repository }}:{{ if $context.Values.global }}{{ $context.Values.global.versions.microservices | default $config.image.tag | default $context.Chart.AppVersion }}{{ else }}{{ $config.image.tag | default $context.Chart.AppVersion }}{{ end }}
        imagePullPolicy: {{ $config.image.pullPolicy | default "IfNotPresent" }}
        securityContext:
          {{- include "reloading.containerSecurityContext" $context | nindent 10 }}
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        {{- include "reloading.microservice.commonEnv" (dict "componentName" $componentName "context" $context) | nindent 8 }}
        {{- if $needsDatabase }}
        {{- include "reloading.microservice.databaseEnv" $context | nindent 8 }}
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
{{- define "reloading.microservice.service" -}}
{{- $componentName := .componentName }}
{{- $serviceName := .serviceName }}
{{- $config := .config }}
{{- $context := .context }}
---
apiVersion: v1
kind: Service
metadata:
  name: {{ $serviceName }}
  namespace: {{ $context.Values.namespace }}
  labels:
    {{- include "reloading.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
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
    {{- include "reloading.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
{{- end }}

{{/*
Generate microservice ingress
*/}}
{{- define "reloading.microservice.ingress" -}}
{{- $componentName := .componentName }}
{{- $config := .config }}
{{- $context := .context }}
{{- if $config.ingress.enabled }}
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "reloading.fullname" $context }}-{{ $componentName }}
  namespace: {{ $context.Values.namespace }}
  labels:
    {{- include "reloading.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
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
                name: {{ include "reloading.fullname" $context }}-{{ $componentName }}
                port:
                  number: {{ $config.service.port | default 8080 }}
          {{- end }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
Pod security context
*/}}
{{- define "reloading.podSecurityContext" -}}
runAsNonRoot: true
runAsUser: {{ .Values.securityContext.runAsUser | default 1000 }}
fsGroup: {{ .Values.securityContext.fsGroup | default 2000 }}
seccompProfile:
  type: RuntimeDefault
{{- end }}

{{/*
Container security context
*/}}
{{- define "reloading.containerSecurityContext" -}}
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
Generate microservice PodDisruptionBudget
*/}}
{{- define "reloading.microservice.podDisruptionBudget" -}}
{{- $componentName := .componentName }}
{{- $config := .config }}
{{- $context := .context }}
{{- if $config.podDisruptionBudget.enabled }}
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: {{ include "reloading.fullname" $context }}-{{ $componentName }}-pdb
  namespace: {{ $context.Values.namespace }}
  labels:
    {{- include "reloading.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
spec:
  {{- if $config.podDisruptionBudget.minAvailable }}
  minAvailable: {{ $config.podDisruptionBudget.minAvailable }}
  {{- else }}
  maxUnavailable: {{ $config.podDisruptionBudget.maxUnavailable | default 1 }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "reloading.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 6 }}
{{- end }}
{{- end }}

{{/*
Generate microservice NetworkPolicy
*/}}
{{- define "reloading.microservice.networkPolicy" -}}
{{- $componentName := .componentName }}
{{- $config := .config }}
{{- $context := .context }}
{{- $needsDatabase := .needsDatabase | default false }}
{{- if $config.networkPolicy.enabled }}
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: {{ include "reloading.fullname" $context }}-{{ $componentName }}-netpol
  namespace: {{ $context.Values.namespace }}
  labels:
    {{- include "reloading.microservice.labels" (dict "componentName" $componentName "context" $context) | nindent 4 }}
spec:
  podSelector:
    matchLabels:
      {{- include "reloading.microservice.selectorLabels" (dict "componentName" $componentName "context" $context) | nindent 6 }}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  {{- if eq $componentName "api-gateway" }}
  # API Gateway accepts traffic from Ingress controller
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  {{- else }}
  # Other services accept traffic from API Gateway
  - from:
    - podSelector:
        matchLabels:
          app.kubernetes.io/component: api-gateway
    ports:
    - protocol: TCP
      port: 8080
  {{- end }}
  egress:
  # Allow DNS resolution
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: UDP
      port: 53
  {{- if eq $componentName "api-gateway" }}
  # API Gateway needs to reach all microservices
  - to:
    - podSelector:
        matchLabels:
          app.kubernetes.io/name: {{ include "reloading.name" $context }}
    ports:
    - protocol: TCP
      port: 8080
  {{- end }}
  {{- if $needsDatabase }}
  # Allow access to PostgreSQL
  - to:
    - namespaceSelector:
        matchLabels:
          name: {{ $context.Values.postgresql.namespace | default "postgres" }}
    ports:
    - protocol: TCP
      port: 5432
  {{- end }}
  # Allow access to Keycloak for OAuth2/OIDC
  - to:
    - namespaceSelector:
        matchLabels:
          name: {{ $context.Values.keycloak.namespace | default "keycloak" }}
    ports:
    - protocol: TCP
      port: 8080
  # Allow access to Discovery Server (Eureka)
  - to:
    - podSelector:
        matchLabels:
          app.kubernetes.io/name: {{ include "reloading.name" $context }}
          app.kubernetes.io/component: discovery-server
    ports:
    - protocol: TCP
      port: 8761
  # Allow access to OpenTelemetry Collector
  - to:
    - namespaceSelector:
        matchLabels:
          name: {{ $context.Values.observability.namespace | default "observability" }}
    ports:
    - protocol: TCP
      port: 4317
    - protocol: TCP
      port: 4318
  # Allow HTTPS for external APIs (e.g., Spring AI)
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 443
{{- end }}
{{- end }}
