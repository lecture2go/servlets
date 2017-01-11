# Video Converter

You can use this simple servlet for the following:
- converting of different video formats to h.264/AAC encodet video (MPEG-4)
- extracting mp3 audio/AAC from MPEG 4-Video

## System Requirements
- FFMPEG
- Webcontainer (e.g. Apache Tomcate)

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
