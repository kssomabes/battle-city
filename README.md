# Battle City
A battle royal version of the classic game, Battle City. 

# Development
This project is developed using Windows with Java. All other guides for installation mentioned are for Windows only.

- Java
- Eclipse Neon
- Protobuf 3.6.1 Java

# Installing Protoc
- Get the Windows release for protobuf from this [link](https://github.com/protocolbuffers/protobuf/releases "link").
- Extract and copy the path.
- Do not forget to update PATH variable.

# Using Protobuf 
1. Get the Java Protobuf from from this [link](https://github.com/protocolbuffers/protobuf/releases "link").
2. Run `protoc --java_out=OUTPUT_DIR INPUT_PROTO`. Replace OUTPUT_DIR with the directory to compile INPUT_PROTO or the .proto file. 
3. Extract the Java Protobuf, go to `java\core\src\main\java` and copy the `com` folder.
4. Copy the `com` folder to the output directory from Step 2.
5. Run `protoc --java_out=OUTPUT_DIR \Path\To\Protoc\Folder\include\google\protobuf\descriptor.proto` 
