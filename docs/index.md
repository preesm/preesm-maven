# Preesm Maven Repo

The `<repository>` to add to your POM file should look like:

```xml
<properties>
  <!-- ... -->
  <preesm-maven-repo>https://preesm.github.io/preesm-maven/mavenrepo/</preesm-maven-repo>
  <!-- ... -->
</properties>

<repositories>
  <!-- ... -->
  <repository>
    <id>Preesm Maven Repo</id>
    <url>${preesm-maven-repo}</url>
  </repository>
  <!-- ... -->
</repositories>
```

Do not forget to also add the following to enable the download of 
checkstyle settings if used:

```xml
<pluginRepositories>
  <pluginRepository>
    <id>Preesm Maven Repo</id>
    <url>${preesm-maven-repo}</url>
  </pluginRepository>
</pluginRepositories>
```
