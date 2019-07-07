import lombok.experimental.SafeCall;
class SafeCallConstants {
  public SafeCallConstants() {
    super();
    @SafeCall int i = 0;
    {
    }
    @SafeCall int iHex = 0x1;
    {
    }
    @SafeCall int iBin = 0b1;
    {
    }
    @SafeCall int iOct = 01;
    {
    }
    @SafeCall byte b = 0;
    {
    }
    @SafeCall short s = 0;
    {
    }
    @SafeCall long l = 0L;
    {
    }
    @SafeCall float f = 0f;
    {
    }
    @SafeCall double d = 0d;
    {
    }
    @SafeCall char c = 'a';
    {
    }
  }
}