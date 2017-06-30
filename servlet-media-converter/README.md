# Video Converter

You can use this simple servlet for the following:
- converting of different video formats to h.264/AAC encodet video (MPEG-4)
- extracting mp3 audio/AAC from MPEG 4-Video

## System Requirements
- FFMPEG
- Webcontainer (e.g. Apache Tomcate)

## Configuration
Open the web.xml configuration file. Here you can set the following parameters:
- converterServerName
- maxQueue (Number of videos converted at the same time)
- ffmpegBin (run "which ffmpeg" on your shell for this parameter)
- ffmpegConvertFileToMp4CommandParameter (Specifies the command to convert)
- ffmpegExtractMp3FileFromMp4 (Specifies the command to extract)

```
		<init-param>
			<param-name>converterServerName</param-name>
			<param-value>localhost</param-value>
		</init-param>
		<init-param>
			<param-name>maxQueue</param-name>
			<param-value>3</param-value>
		</init-param>
		<init-param>
			<param-name>ffmpegBin</param-name>
			<param-value>/usr/local/bin/ffmpeg</param-value>
		</init-param>
	    <init-param>
	      	<param-name>ffmpegConvertFileToMp4CommandParameter</param-name>
	      	<param-value>-f mp4 -c:v libx264 -preset slow -b:v 650k -r 25 -bufsize 175k -strict -2 -c:a aac -ar 44100 -b:a 64k -ac 1</param-value>
	    </init-param>
	    <init-param>
	      	<param-name>ffmpegExtractMp3FileFromMp4</param-name>
	      	<param-value>-f mp3 -vn</param-value>
	    </init-param>
      
```

## How to install
Copy this servlet to your servlet container root directory.

## Run
- For MP4 converting
```
localhost:8080/convertFile?action=convertFileToMp4&id=/path/to/your/video.extention
```

- For MP3 extraction 
```
localhost:8080/convertFile?action=extractMp3FromMp4&id=/path/to/your/video.mp4
```
