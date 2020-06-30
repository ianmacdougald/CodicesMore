Composite {
	classvar <directory, id = 'sc-modules', <dictionary;
	var <moduleSet, <modules, <templater;

	*new { | moduleSet(\default), from |
		this.checkDefaults;
		^super.newCopyArgs(moduleSet).processModules(from).initComposite;
	}

	*basicNew { | moduleSet(\default), from |
		^super.newCopyArgs(moduleSet);
	}

	*initClass {
		Class.initClassTree(Dictionary);
		Class.initClassTree(PathStorage);
		Class.initClassTree(FolderManager);
		Class.initClassTree(ModuleDictionary);
		directory = PathStorage.at(id) ?? {
			PathStorage.setAt(this.defaultDirectory, id);
		};
		dictionary = ModuleDictionary.new;
		//folderManager = FolderManager.new(this.directory);
	}

	*defaultDirectory {
		^(Main.packages.asDict.at('CodexIan')+/+id);
	}

	*checkDefaults {
		var defaults = this.defaultModulePath; 
		var folder = this.classFolder+/+"default";
		if(defaults.exists and: { folder.exists.not }, { 
			defaults.copyScriptsTo(folder.mkdir)
		});
	}

	*defaultModulePath { ^""; }

	processModules { | from |
		var klass = this.class, dict = klass.dictionary;
		templater = Templater(this.moduleFolder);
		if(dict.notInDictionary(klass.name, moduleSet), {
			from !? {
				this.copyFrom(from);
				forkIfNeeded { this.processFolders(from); };
			} ?? {
				this.processFolders;
				dict.addEntry(klass.name, moduleSet, this.loadModules);
			};
		});
		modules = dict.modulesAt(klass.name, moduleSet).copy;
	}

	copyFrom { | from |
		var klass = this.class, dict = klass.dictionary;
		if(dict.notInDictionary(klass.name, from), {
			dict.addEntry(klass.name, from, this.loadFrom(from));
		});
		dict.copyEntry(klass.name, from, moduleSet);
	}

	initComposite {}

	moduleFolder { ^(this.class.classFolder+/+moduleSet); }

	folderFrom { | from | ^(this.class.classFolder+/+from); }

	*classFolder { ^(this.directory +/+ this.name); }

	processFolders { | from |
		if(this.moduleFolder.exists.not, {  
			from !? {
				(this.class.classFolder+/+from)
				.copyScriptsTo(this.moduleFolder);
			} ?? { this.makeTemplates; };
		});
	}

	makeTemplates {
		this.subclassResponsibility(thisMethod);
	}

	loadFrom { | from | ^this.getModules(this.folderFrom(from)); }

	loadModules { ^this.getModules(this.moduleFolder); }

	getModules { | folder |
		^folder.getScriptPaths.collect({ | script |
			[this.getModuleName(script), script.load];
		}).flat.asPairs(Event);
	}

	reloadModules {
		var klass = this.class, dict = klass.dictionary;
		dict.removeModules(klass.name, moduleSet);
		this.getModules;
	}

	getModuleName { | input |
		^PathName(input)
		.fileNameWithoutExtension
		.lowerFirstChar.asSymbol;
	}

	*directory_{| newPath |
		directory = PathStorage.setAt(newPath, id);
	}

	moduleSet_{| newSet, from |
		moduleSet = newSet;
		this.processModules(from);
		this.initComposite;
	}

	*moduleSets {
		^PathName(this.classFolder).folders
		.collectAs({|m|m.folderName.asSymbol}, Set);
	}
}
