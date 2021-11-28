# Chamber-Subtree-Module


## Commands to Add, Push and Pull

### Add in your app
#### Add using SSH
```
git subtree add --prefix chamber git@github.com:yogeshpaliyal/Chamber-Subtree-Module.git master --squash
```

#### Add using HTTPS
```
git subtree add --prefix chamber https://github.com/yogeshpaliyal/Chamber-Subtree-Module.git master --squash
```
### Pull module code
```
git subtree pull --prefix chamber git@github.com:yogeshpaliyal/Chamber.git master --squash
```

### Push to module
```
git subtree push --prefix chamber git@github.com:yogeshpaliyal/Chamber.git master
```

Having issue check this link => [Reference](https://gist.github.com/SKempin/b7857a6ff6bddb05717cc17a44091202)


## Add module in `settings.gradle`
```
include ':chamber'
```

## implement module in `build.gradle` (app level)
```
implementation project(":chamber")
```
