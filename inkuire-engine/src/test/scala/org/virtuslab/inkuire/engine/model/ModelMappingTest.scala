package org.virtuslab.inkuire.engine.model

import org.virtuslab.inkuire.engine.BaseInkuireTest

class ModelMappingTest extends BaseInkuireTest {
  it should "map global function" in {
    //given
    val source = "{\n   \"dri\":\"/////\",\n   \"name\":\"example\",\n   \"packages\":[\n      {\n         \"dri\":\"example/////\",\n         \"name\":\"example\",\n         \"functions\":[\n            {\n               \"dri\":\"example//main/#//\",\n               \"name\":\"main\",\n               \"isConstructor\":false,\n               \"parameters\":[\n\n               ],\n               \"generics\":[\n\n               ]\n            }\n         ],\n         \"properties\":[],\n         \"classlikes\":[],\n         \"typealiases\":[]\n     }\n ]\n}"

    //when
    val inkuire = InkuireDb.read(source)

    //then
    val expected = Seq(
      ExternalSignature(Signature(
        None, Seq.empty, ConcreteType("Unit"), SignatureContext(Set.empty, Map.empty)), "main", "example/////")
    )
    inkuire.functions should matchTo(expected)
  }
}
