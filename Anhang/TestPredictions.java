/**
 * Verwendet drei Mahlzeiten und drei Zeitpunkte und berechnet fuer alle Mahlzeiten zu allen Zeiten die Ableitungen
 * zur Groesse der Mahlzeit und zum Startpunkt der Mahlzeit. Das gewuenschte Ergebnis wurde zuvor per Hand berechnet
 * und hier wird ueberprueft, dass die Implementation der Funktion das selbe Ergebnis liefert.
 */
@Test
public void jacobiTest() {
    RealVector mealValues = new ArrayRealVector(new double[]{30.0, 20.0, 10.0});
    RealVector mealTimes = new ArrayRealVector(new double[]{0, 40, 100});
    RealVector testTimes = new ArrayRealVector(new double[]{10, 50, 120});

    RealMatrix result = Predictions.jacobi(testTimes, mealTimes, mealValues, sens, carbRatio, absorptionTime);

    // erster Zeitpunkt
    assertEquals(-7.0 / 24.0, result.getEntry(0, 0), 1e-15);    // nach Zeitpunkt der ersten Mahlzeit abgeleitet
    assertEquals(0, result.getEntry(0, 1));                     // nach Zeitpunkt der zweiten Mahlzeit abgeleitet
    assertEquals(0, result.getEntry(0, 2));                     // nach Zeitpunkt der dritten Mahlzeit abgeleitet
    // zweiter Zeitpunkt
    assertEquals(-35.0 / 24.0, result.getEntry(1, 0), 1e-15);   // usw...
    assertEquals(-7.0 / 36.0, result.getEntry(1, 1), 1e-15);
    assertEquals(0, result.getEntry(1, 2));
    // dritter Zeitpunkt
    assertEquals(0, result.getEntry(2, 0));
    assertEquals(-7.0 / 9.0, result.getEntry(2, 1), 1e-15);
    assertEquals(-7.0 / 36.0, result.getEntry(2, 2), 1e-15);

    // erster Zeitpunkt
    assertEquals(7.0 / 144.0, result.getEntry(0, 3), 1e-15);    // nach Groesse der ersten Mahlzeit abgeleitet
    assertEquals(0, result.getEntry(0, 4));                     // nach Groesse der zweiten Mahlzeit abgeleitet
    assertEquals(0, result.getEntry(0, 5));                     // nach Groesse der dritten Mahlzeit abgeleitet
    // zweiter Zeitpunkt
    assertEquals(175.0 / 144.0, result.getEntry(1, 3), 1e-15);  // usw...
    assertEquals(7.0 / 144.0, result.getEntry(1, 4), 1e-15);
    assertEquals(0, result.getEntry(1, 5));
    // dritter Zeitpunkt
    assertEquals(35.0 / 10.0, result.getEntry(2, 3));
    assertEquals(49.0 / 18.0, result.getEntry(2, 4), 1e-15);
    assertEquals(7.0 / 36.0, result.getEntry(2, 5), 1e-15);
}

/**
 * Tested die gleiche Funktion wie der vorherige Test mit zufaellig generierten
 * Datenpunkten. Zur Berechnung des erwarteten Ergebnis wird die unten stehende
 * private Funktion verwendet.
 */
@Test
public void randomJacobiTest() {
    int sizeTimes = 3 + random.nextInt(3);
    RealVector testTimes = new ArrayRealVector(sizeTimes);

    int sizeMeals = 1 + random.nextInt(3);
    RealVector mealValues = new ArrayRealVector(sizeMeals);
    RealVector mealTimes = new ArrayRealVector(sizeMeals);

    int oldDate = 0;
    for (int i = 0; i < sizeTimes; i++) {
        int newDate = 1 + random.nextInt(120) + oldDate;
        testTimes.setEntry(i, newDate);
        oldDate = newDate;
    }
    oldDate = 0;

    for (int i = 0; i < sizeMeals; i++) {
        int newDate = 1 + random.nextInt(120) + oldDate;
        mealTimes.setEntry(i, newDate);
        mealValues.setEntry(i, 1 + random.nextInt(50));
        oldDate = newDate;
    }
    RealMatrix result = Predictions.jacobi(testTimes, mealTimes, mealValues, sens, carbRatio, absorptionTime);
    for (int i = 0; i < sizeMeals; i++) {
        RealVector r = result.getColumnVector(i);
        assertEquals(sizeTimes, r.getDimension());
        for (int j = 0; j < sizeTimes; j++) {
            assertEquals(cob_dtMeal(testTimes.getEntry(j), mealTimes.getEntry(i), mealValues.getEntry(i), sens, carbRatio, absorptionTime), r.getEntry(j));
        }
    }
    for (int i = 0; i < sizeMeals; i++) {
        RealVector r = result.getColumnVector(i + sizeMeals);
        assertEquals(sizeTimes, r.getDimension());
        for (int j = 0; j < sizeTimes; j++) {
            assertEquals(sens / carbRatio * Predictions.carbsOnBoard(testTimes.getEntry(j) - mealTimes.getEntry(i), absorptionTime), r.getEntry(j));
        }
    }
}

private static double cob_dtMeal(double time, double mealTime, double carbsAmount, double insSensitivityFactor, double carbRatio, long absorptionTime) {
    double c = insSensitivityFactor / carbRatio * carbsAmount * 4 / absorptionTime;
    double deltaTime = time - mealTime;
    if (deltaTime < 0 || deltaTime > absorptionTime) {
        return 0;
    } else if (deltaTime < absorptionTime / 2.0) {
        return -c * deltaTime / absorptionTime;
    } else if (deltaTime >= absorptionTime / 2.0) {
        return c * (deltaTime / absorptionTime - 1);
    }
    return 0;
}