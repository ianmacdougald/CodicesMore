Hybrid : Modular {
	classvar isInit = false, <dictionary, processor;
	var <server;

	*new {|moduleName, from|
		if(isInit.not, {this.initHybrid});
		^super.new(moduleName, from);
	}

	*initHybrid {
		dictionary = Dictionary.new;
		processor = SynthDefProcessor.new;
		ServerQuit.add({this.clearDictionary});
		isInit = true;
	}

	*clearDictionary {
		dictionary.do({|subD| processor.remove(subD.asArray)});
	}

	initHybrid {
		server = server ? Server.default;
		if(this.class.subDictionaryExists.not, {
			this.class.addSubDictionary;
		});
		this.makeSynthDefs;
	}

	*subDictionaryExists {|className|
		^dictionary[this.name].notNil;
	}

	*addSubDictionary {
		dictionary[this.name] = Dictionary.new;
	}

	makeSynthDefs {
		var toProcess = [];
		modules.do({|module|
			toProcess = toProcess.add(this.checkModule(module));
		});
		this.class.processSynthDefs(toProcess.flat);
	}

	checkModule { |object|
		var synthDefs = [];
		case
		{object.isCollection and: {object.isString.not}}{
			object.do({|item| ^this.checkModule(item)});
		}
		{object.isFunction}{^this.checkModule(object.value)}
		{object.isKindOf(SynthDef)}{
			object.name = this.formatName(object).asSymbol;
			if(this.checkDictionary(object), { 
				synthDefs = synthDefs.add(object);
			});
		};
		^synthDefs;
	}

	checkDictionary { |synthDef|
		var class = this.class;
		if(class.notInDictionary(synthDef), { 
			class.addToDictionary(synthDef);
			^true;
		}); 
		^false;
	}

	*notInDictionary { |synthDefName| 
		^dictionary[this.name][synthDefName].isNil;
	}

	*addToDictionary { |synthDef|
		dictionary[this.name].add(synthDef.name -> synthDef);
	}

	formatName { |object|
		^this.tagName(this.class.name, this.tagName(moduleName, object.name));
	}

	tagName {|tag, name|
		tag = tag.asString; name = name.asString;
		if(name.contains(tag).not, { 
			name = format("%_%", tag, name);
		});
		^name;
	}

	*processSynthDefs { |synthDef|
		processor.add(synthDef);
	}

	server_{|newServer|
		server = server ? Server.default;
	}

	*clearSynthDefs {
		var toRemove = dictionary.removeAt(this.name);
		toRemove !? {processor.remove(toRemove.asArray)};
	}

	loadModules { 
		super.loadModules; 
		this.initHybrid;
	}

	moduleName_{|newModule, from|
		moduleName = newModule;
		this.initModular(from);
	}
}
