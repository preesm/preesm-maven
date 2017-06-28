# sftp-maven-plugin
Maven plugin for transfering files over SFTP, using local maven settings for username and password.

Default configuration looks as follows:

```xml
<plugin>
	<groupId>org.ietr.maven</groupId>
	<artifactId>sftp-maven-plugin</artifactId>
	<version>2.1.0</version>
	<executions>
		<execution>
			<id>test-receive</id>
			<phase>initialize</phase>
			<configuration>
				<serverId>server.id</serverId>
				<serverHost>server.host.or.ip</serverHost>
				<!-- true by default -->
				<strictHostKeyChecking>false</strictHostKeyChecking>
				<!-- 8 by default -->
				<transferThreadCount>2</transferThreadCount>
				<!-- send or receive, receive by default -->
				<mode>receive</mode>
				<remotePath>/absolute/path/on/remote/host</remotePath>
				<localPath>relative/or/absolute/local/path/</localPath>
			</configuration>
			<goals>
				<goal>sftp-transfert</goal>
			</goals>
		</execution>
		<execution>
			<id>test-send</id>
			<phase>initialize</phase>
			<configuration>
				<serverId>server.id</serverId>
				<serverHost>server.host.or.ip</serverHost>
				<strictHostKeyChecking>false</strictHostKeyChecking>
				<transferThreadCount>16</transferThreadCount>
				<mode>send</mode>
				<remotePath>/home/projects/myprojects/htdcs/p2repo/</remotePath>
				<localPath>${project.basedir}/target/repository/</localPath>
			</configuration>
			<goals>
				<goal>sftp-transfert</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

Available through Preesm Maven repository:

```xml
<pluginRepository>
	<id>preesm</id>
	<url>https://preesm.github.io/preesm-maven/mavenrepo/</url>
</pluginRepository>
```
