#import py4j
from py4j.java_gateway import JavaGateway

gateway  = JavaGateway()
java_object = gateway.jvm.minicpbp.bridge.EntryPoint()
print(java_object.test())


print("test")
