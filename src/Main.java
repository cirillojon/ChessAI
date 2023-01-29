public class Main {

    //Create a method that checks if a number is prime
    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i < n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
    //Create a method that sums the prime numbers up to 50
    public static int sumPrimeNumbers(int n) {
        int sum = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrime(i)) {
                sum += i;
            }
        }
        return sum;
    }
    public static void main(String[] args) {
        System.out.println("Hello world!");

        //Print the sum of the prime numbers up to 50
        System.out.println(sumPrimeNumbers(50));

    }
}