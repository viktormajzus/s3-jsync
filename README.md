# s3-jsync

A basic tool to sync up with an S3 bucket.

## Build Instructions

### Clone this repository anywhere
```
git clone https://github.com/viktormajzus/s3-jsync
cd s3-jsync
```

### All Platforms
Requires **JDK 21**. Gradle will automatically install it when you build, if it is not installed

### Linux (to build installer)
- For **Debian/Ubuntu** (to build .deb)
  ```
  sudo apt install dpkg-dev fakeroot
  ./gradlew jpackage -Ppkg=deb
  sudo apt install ./build/jpackage/s3-jsync_1.1.0_amd64.deb
  ```
- For **RHEL/Rocky/Fedora** (to build .rpm)
  ```
  sudo dnf install rpm-build
  ./gradlew jpackage -Ppkg=rpm
  sudo dnf install ./build/jpackage/s3-jsync-1.1.0-1.x86_64.rpm
  ```

Config will be created upon your first run of the program. It will be saved in home/s3-jsync/config.cfg

### Windows
Requires **WiX v3.XX** on PATH in order to build the exe/msi installers
- For **MSI** installer
  ```
  .\gradlew jpackage -Ppkg=msi
  ```
- For **EXE** installer
  ```
  .\gradlew jpackage -Ppkg=exe
  ```

Config will be created upon your first run of the program. It will be saved in %LOCALAPPDATA%/s3-jsync/config.cfg

### Or, simply, run the provided scripts in /packaging/
Note that you will still need to manually install the program by either running the msi/exe, or using sudo apt/dnf install

### Build it and enjoy!

### Recommended:
Add the folder to your Path environment variable:
Windows Key -> Edit the system environment variables -> Environment variables -> Select Path -> Press Edit -> New -> C:\PATH\TO\s3-jsync folder

## Usage Instructions

### Configuration
You will need to set the following environment variables to their respective values:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_KEY`
- `AWS_REGION`

You can do so with these commands:

- **Linux**:
  ```
  export AWS_ACCESS_KEY_ID=AKIA....MT
  export AWS_SECRET_KEY=8u....zq
  export AWS_REGION=us-east-1
  ```
- **Windows**:
  ```
  setx AWS_ACCESS_KEY_ID AKIA....MT
  setx AWS_SECRET_KEY 8u....zq
  setx AWS_REGION us-east-1
  ```
Note that the above command for windows will set the variables permanently. If you want to use these variables only in the current session, use `set` instead of `setx`

You can also change the environment variables by going to:

Windows Key -> Edit the system environment variables -> Environment variables -> Select Path -> Press Edit -> New -> C:\PATH\TO\s3-jsync folder

### Upload Files
`s3-jsync put <SOURCE/FOLDER> <DESTINATION_BUCKET>`

Will upload every single file within this folder into the destination bucket.

**WARNING** Overwrites files based on their date modified

### Download Files
`s3-jsync get <SOURCE_BUCKET> <DESTINATION_FOLDER>`

Downloads files from the source bucket to the destination folder.

**WARNING** Overwrites files based on their date modified

### List buckets
`s3-jsync list -b`

Lists every bucket that the user (with the access key) has access to see

### List objects
`s3-jsync list -o <SOURCE_BUCKET>`

Lists every object within a specified bucket

### Wipe a bucket
`s3-jsync delete <DESTINATION_BUCKET>`

Deletes every object in a specified bucket.

**WARNING** Dangerous command, try not to use it, unless you know what you're doing

### Help menu
`s3-jsync help`

`s3-jsync --help`

Opens a help menu

### Configure [DEPRECATED]
`s3-jsync configure`

Does nothing, as it was deprecated, and the credentials were moved to environment variables
