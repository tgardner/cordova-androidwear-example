GRADLE = gradlew
CWD = $(shell pwd)
CORDOVA = $(shell which cordova)
CORDOVA_VERSION = $(shell cordova -v | sed -e 's/\..*//g')
CORDOVA_PROJECT = $(CWD)/phone
WEAR_PROJECT = $(CWD)/wear
ANDROID_PLATFORM = $(CORDOVA_PROJECT)/platforms/android
WEAR_OUTPUT = wearable_app

PACKAGE_NAME = $(shell cat "$(WEAR_PROJECT)/app/build.gradle" | grep applicationId | sed -e 's/.*applicationId //g' -e 's/\"//g')
VERSION_CODE = $(shell cat "$(WEAR_PROJECT)/app/build.gradle" | grep versionCode | sed -e 's/.*versionCode //g' -e 's/\"//g')
VERSION_NAME = $(shell cat "$(WEAR_PROJECT)/app/build.gradle" | grep versionName | sed -e 's/.*versionName //g' -e 's/\"//g')

MANIFEST_BODY = <meta-data android:name=\"com.google.android.wearable.beta.app\" android:resource=\"@xml\/wearable_app_desc\" \/>

define WEARABLE_CONFIG_BODY
<wearableApp package='${PACKAGE_NAME}'>
	<versionCode>$(VERSION_CODE)</versionCode>
	<versionName>$(VERSION_NAME)</versionName>
	<rawPathResId>$(WEAR_OUTPUT)</rawPathResId> 
</wearableApp>
endef
export WEARABLE_CONFIG_BODY

.PHONY: cordova_prepare clean wear config_phone phone

all: cordova_prepare clean wear config_phone phone

cordova_prepare:
	if [ ${CORDOVA_VERSION} -lt 5 ] ; then \
		echo "You must have at least cordova 5.0.0. Please run 'npm update -g cordova'" && \
		exit 1 ; \
	fi
	cd "$(CORDOVA_PROJECT)" && $(CORDOVA) prepare

config_phone: 
	mkdir -p "$(ANDROID_PLATFORM)/res/raw"
	cp -f "$(WEAR_PROJECT)/app/build/outputs/apk/app-debug.apk" "$(ANDROID_PLATFORM)/res/raw/$(WEAR_OUTPUT).apk"
	echo "$$WEARABLE_CONFIG_BODY" > "$(ANDROID_PLATFORM)/res/xml/wearable_app_desc.xml"
	sed -i '' "s/$(MANIFEST_BODY)//g" "$(ANDROID_PLATFORM)/AndroidManifest.xml"
	sed -i '' "s/<\/application>/$(MANIFEST_BODY)<\/application>/g" "$(ANDROID_PLATFORM)/AndroidManifest.xml"

phone:
	cd "$(ANDROID_PLATFORM)" && ./cordova/build

wear:
	cd "$(WEAR_PROJECT)" && ./$(GRADLE) assembleDebug

install:
	adb -d install -r "$(ANDROID_PLATFORM)/build/outputs/apk/android-debug.apk"

clean:
	cd "$(WEAR_PROJECT)" && ./$(GRADLE) clean
	cd "$(ANDROID_PLATFORM)" && ./cordova/clean
	rm -f "$(CORDOVA_PROJECT)/res/raw/$(WEAR_OUTPUT).apk"
