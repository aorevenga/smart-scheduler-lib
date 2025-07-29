Instalar las librerías en el repositorio local de maven

mvn install:install-file -DgroupId=MITyCLibAPI -DartifactId=MITyCLibAPI -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibAPI-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=MITyCLibCert -DartifactId=MITyCLibCert -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibCert-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=MITyCLibCrypt -DartifactId=MITyCLibCrypt -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibCrypt-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=MITyCLibOCSP -DartifactId=MITyCLibOCSP -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibOCSP-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=MITyCLibPolicy -DartifactId=MITyCLibPolicy -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibPolicy-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=MITyCLibXADES -DartifactId=MITyCLibXADES -Dversion=1.1.7 -Dpackaging=jar -Dfile=MITyCLibXADES-1.1.7.jar -DgeneratePom=true
mvn install:install-file -DgroupId=xmlsec -DartifactId=xmlsec -Dversion=1.4.2-ADSI -Dpackaging=jar -Dfile=xmlsec-1.4.2-ADSI-1.1.jar -DgeneratePom=true