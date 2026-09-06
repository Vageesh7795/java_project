FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

COPY target/demo.jar /deployments/demo.jar

EXPOSE 8080

CMD ["java", "-jar", "/deployments/demo.jar"]
