# 消息发送策略文档

1.在 `src/main/java/com/bjfu/cms/service/strategy` 包下，新建一个类。 **命名规范**：`角色名 + MessageStrategy`

这里需要严格遵守命名规范，否则无法正确扫描到你的策略，详情见message的service层

你需要重写的方法是checkPermission（我暂时脑子里只想到这个方法要重写）

## 如果你有新的方法需要实现

## **而这个方法又不在父类里面**

## 请一定先跟我说

## ！！！

我们需要先重写其他方法才行。

2.如果你的角色只涉及到发送系统邮件，而不涉及到内部消息，直接去看EmailService就好，前面说的都和你没关系。

3.如果你是写内部消息的，写好strategy之后，你需要调的接口统一都在messageController里面，我们的代码逻辑是：

messageController-》messageService-》寻找你命名的strategy

