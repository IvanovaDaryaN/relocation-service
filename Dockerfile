ARG IMAGE_JRE=bellsoft/liberica-runtime-container:jre-17-slim-musl

FROM ${IMAGE_JRE} AS layers
WORKDIR /layers
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM ${IMAGE_JRE}
WORKDIR /workspace
COPY --from=layers /layers/dependencies/ .
COPY --from=layers /layers/snapshot-dependencies/ .
COPY --from=layers /layers/spring-boot-loader/ .
COPY --from=layers /layers/application/ .
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
