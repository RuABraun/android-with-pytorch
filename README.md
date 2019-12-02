This shows how to use a pytorch model in C++ in an Android app.

Running this will require compiling pytorch for android and setting `TORCHPATH` in `app/src/main/cpp/CMakeLists.txt`.


The model was created like this:

```
import torch as to
import torch.nn as nn


class Mod(nn.Module):
    def __init__(self):
        super().__init__()
        self.fc = nn.Linear(3, 2)

    def forward(self, x):
        return self.fc(x)


def main():
    model = Mod()
    x = to.rand(1, 3)
    traced_module = to.jit.trace(model, x)

    traced_module.save('traced_model.pt')

main()
```
