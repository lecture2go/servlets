# File Download
You can use this simple servlet for the following:
- servlet based file donwnload

## System Requirements
- Webcontainer (e.g. Apache Tomcate)

## Configuration
Open the web.xml configuration file. Here you can set the following parameters:
- downloadServerName
- repositoryRoot (path to your repository)

```
		<init-param>
			<param-name>repositoryRoot</param-name>
			<param-value>/path/to/your/repository</param-value>
		</init-param>
		<init-param>
			<param-name>downloadServerName</param-name>
			<param-value>localhost</param-value>
		</init-param>
      
```

## How to install
Copy this servlet to your servlet container root directory.

## Run
```
localhost:8080/servlet-file-download/getFile?downloadAllowed=1&downloadPath=/path/to/your/file.extentionn
`
