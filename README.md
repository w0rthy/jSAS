# jSAS
Java Syntax Altered Script

The major difference java is the ability to redefine operators and keywords and not requiring to have a class.

Having a file with just "System.out.println("helloworld");" in it will compile and run.

All loose code that isnt in a class or a method will automatically be put into the main method of a generic class
  by the jSAS translator
  
Any methods not in a class will also be placed in the generic class.

All classes in the file will be handled properly and will be accessible like normal.


Jar use, first argument is path to file ex. "C:/Stuff/helloworld.jsas"

  second argument is one of 3 options:
  
    "trans" this translates the file to a valid java file
    
    "comp" this translates and compiles the file in java
    
    "run" this translates, compiles, and runs the file
    
  the last argument is the path to where the .java will be translated ex. "C:/Java/jSAStest1.java"
  
  
# imports and packages must be done through the hash commands
  the hash commands are:
    "#redef" "#def" redfine java operators and keywords to whatever you wish and define new ones. ex. "#redef boolean bool"
        the keyword bool now behaves exactly the same as java's boolean. You can also redefine mathematical operators.
    
    "#import" "#imp" used to import like you normally would in java. To import everything in io you would simply "#imp java.io.*"
    
    "#staticimport" "#simp" same as above but imports with the static keyword
    
    "#package" "#pack" declares the package of the file
    
    "#time" time how long the program takes to run. There are 3 possible choices for time measurement "nan" for nano,
          "mil" for milis, "sec" for seconds.
