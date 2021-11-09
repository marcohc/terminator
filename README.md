# Terminator

Is an opinionated Android framework based on a MVI (Model-View-Intention) pattern. Besides the
framework, it contains all the necessary tools which makes your development more testable, faster,
better and more cohesive.

## Configuration

Latest
version: [![](https://jitpack.io/v/marcohc/terminator.svg)](https://jitpack.io/#marcohc/terminator)

### Step1

Add it in your root build.gradle at the end of repositories:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step2

Add the dependency on your modules build.gradle file:

```
dependencies {
	        implementation 'com.github.marcohc:terminator:<version>'
	}
```

## Architecture

A great explanation of MVI:

- MVI architecture: https://proandroiddev.com/the-contract-of-the-model-view-intent-architecture-777f95706c1e
- DroidconNYC 2027: https://www.youtube.com/watch?v=PXBXcHQeDLE

## Modules

TODO

# Credit to

Benoît Quenaudon (https://github.com/oldergod). His video, article and repository about MVI made me
create this framework. Thanks!
