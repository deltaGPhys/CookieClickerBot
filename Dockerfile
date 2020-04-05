FROM openjdk:8
COPY . /
WORKDIR /src/main/java
RUN javac Main.java
CMD ["java", "Main"]
