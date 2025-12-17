//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    GenerateToken.MyThreadGenerator thread1 = new GenerateToken.MyThreadGenerator();
    GenerateToken.MyThreadGenerator thread2 = new GenerateToken.MyThreadGenerator();
    GenerateToken.MyThreadGenerator thread3 = new GenerateToken.MyThreadGenerator();
    GenerateToken.MyThreadGenerator thread4 = new GenerateToken.MyThreadGenerator();
    GenerateToken.MyThreadGenerator thread5 = new GenerateToken.MyThreadGenerator();
    GenerateToken.MyThreadGenerator thread6 = new GenerateToken.MyThreadGenerator();

    thread1.start();
    thread2.start();
    thread3.start();
    thread4.start();
    thread5.start();
    thread6.start();
}
