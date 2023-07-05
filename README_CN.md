# 奇幻小马国

[![Downloads](https://img.shields.io/github/downloads/Sollace/Unicopia/total.svg?color=yellowgreen)](https://github.com/Sollace/Unicopia/releases/latest)
![](https://img.shields.io/badge/api-fabric-orange.svg)

将友谊的的魔法带到Minecraft世界中！

起初，本模组只是一款能让玩家体验扮演魔法独角兽的简陋程序，但现已进化到成熟阶段。现在您可以在进入Minecraft世界时从各族小马中任选其一作为您的新身份，以此体验全新魔法机制，就如同真正踏入小马国一般！

# 独家特性

## 来看看成为您最喜欢的小马种族是什么感觉

独角兽，天马，陆马，甚至幻形灵都有其独特的能力
 
 - *成为独角兽，* 学习使用魔法！制成您的第一本魔法书并开展实验。既可研究不同魔咒
   的配方与功效，亦可潜心研究历史传说，揭开这奇幻世界过往的面纱！
  
   除了施放保护自己的防护罩，用魔法镭射焚烧敌人的魔咒外，独角兽还可利用瞬移法术免去翻山越岭的苦恼，直接传送到难以到达的地方。
 
 - *成为天马，*统治天空！除了能飞以外，天马还可以制造彩虹音爆、用罐子收容坏天气，
   相比于其他种族可触距离更远，跑得更快。
 
 - *成为兢兢业业的背景小马！*陆马相比于其他种族更壮实。他们亦有
   踢树落果和滋养作物的实用能力。成为陆马，从此再也不缺粮。

 想要加入黑暗阵营吗？

 - *成为幻形虫巢的一员，*可变形成一切事物。掠夺并享用从别的玩家或生物身上吸来
   的爱。变成某些形态时可以使用其形态对应的特殊能力。
  
 - *拥抱暗夜，*成为夜骐。夜骐能发出无止境的尖叫，自带夜视，亦可飞行！唯一的不足之处是在白天时得戴着
   略显冷酷的墨镜免得阳光刺眼。我倒是觉得挺公平，您怎么看？
  
### 管理饮食

  小马们的生活可不只踢树或彩虹镭射！作为食草动物，您得吃普通玩家不常吃的东西。感觉饿了吗？ 试试从草地上摘些花或
  弄点干草来吃！我听说干草堡挺不错的，前提是您能找到一些燕麦。

### 马国艺术
  
  没这些东西怎么还算得上小马模组？每个种族都至少有一种代表他们的艺术品，
  自豪地打出您的旗号吧！
  
  免责声明：不包含彩虹旗（目前）

### 自然现象

  - 突发气流（低精度）
    天马啊，小心在风雨中飞行！这可能会很危险！
    如果您是一种飞行生物，或者穿着飞行道具，请制作一个风向标。
    它能高精度地展示真实的Minecraft世界气流方向。请小心，
    风向和风强 （坏得很）可能会随着环境和您所处位置的变化而变化。

  - 天行者乘势崛起
    不，这绝对不是星战梗，而是一个真实机制。沙子和岩浆将会给予飞行生物一些飞行升力。 水则会起到相反作用。
    您可以试试！还是别了吧，我可不希望您淹死。

### 蕴魔物品和法器
  
  - 为水晶之心盖一座圣殿以为您的朋友们提供宝贵的支援。
  - 或把联谊手环送给您的非独角兽伙伴们，以便他们借用您的力量。
     亦或是带着他们来个突击传送
  - 使用龙息卷轴邮寄物品
  - 还有更多，但是我忘了（真的吗？OoOoOooOOoo…超级惊喜机制）

觉得本描述有问题？还是发现了游戏问题，有甚阙漏？
请在discord上私信我。
事物都是处于运动和变化中的，本描述也有可能过时。

# 玩法

详情请见HOW_TO_PLAY.md文档。

# 依赖项与开发构建

### 仅1.19.3

This project uses reach-entity-attributes, which may not be updated at the time of this writing.
If you building for 1.19.3, you may follow these steps to make sure it's available to git:

`git clone https://github.com/Sollace/reach-entity-attributes`
`cd reach-entity-attributes`
`gradlew build publishToMavenLocal`

### 开发构建奇幻小马国

`git clone https://github.com/Sollace/Unicopia`
`cd Unicopia` 
`gradlew build`

Built jars are located in /build/bin` within the Unicopia folder after performing the above two command.

