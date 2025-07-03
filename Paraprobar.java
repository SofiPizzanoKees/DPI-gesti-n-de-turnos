package com.mycompany.paraprobar;

import java.util.Scanner;

/**
 *
 * @author S 
 */
public class Paraprobar {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Ingrese N1: ");
            int n1 = scanner.nextInt();
            
            System.out.print("Ingrese N2: ");
            int n2 = scanner.nextInt();
            
            int suma = n1 + n2;
            System.out.println("La suma de " + n1 + " y " + n2 + " es " + suma);
        }
    }
}
