FROM tomcat:9.0-jdk11-openjdk

RUN apt update && apt install maven -y

RUN rm -rf /usr/local/tomcat/webapps/*

RUN mkdir /project

WORKDIR /project

COPY . /project

RUN mvn clean package

RUN cp /project/target/user-management.war /usr/local/tomcat/webapps/ROOT.war

WORKDIR /usr/local/tomcat/doc

EXPOSE 8080

CMD ["catalina.sh", "run"]