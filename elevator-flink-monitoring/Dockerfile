FROM maven:3.9.9-eclipse-temurin-11 AS builder
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM flink:1.17.2-scala_2.12-java11
COPY --from=builder /build/target/regtcn-pipeline-1.0.0.jar /opt/flink/usrlib/regtcn-pipeline.jar
