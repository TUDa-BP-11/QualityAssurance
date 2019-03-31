/**
 * Testet, dass nach Ausfuehrung von adjustBasalTreatments() keine ueberschneidungen von Basal Treatments mehr vorhanden sind.
 * Dafuer werden am Anfang bis zu 30 zufaellige Basal Treatments generiert, die alle die Moeglichkeit haben sich um zufaellige Zeitraeume zu ueberschneiden.
 */
@Test
public void testRandomDuration() {
    List<VaultEntry> testList = new ArrayList<>();
    Random random = new Random();
    int size = 1 + random.nextInt(30);
    Date date = new Date(0);
    for (int i = 0; i < size; i++) {
        date = new Date(date.getTime() + ((1 + random.nextInt(12)) * 5 * ONE_MINUTE)); // in steps of 5 min
        VaultEntry entry = new VaultEntry(VaultEntryType.BASAL_MANUAL, date, random.nextDouble() * 100, random.nextInt(6) * 10);
        testList.add(entry);
    }

    List<VaultEntry> resultList = BasalCalculatorTools.adjustBasalTreatments(new ArrayList<>(testList));
    assertEquals(testList.size(), resultList.size());

    for (int i = 0; i < size - 1; i++) {
        long deltaTime = ((testList.get(i + 1).getTimestamp().getTime() - testList.get(i).getTimestamp().getTime()) / ONE_MINUTE);
        if (testList.get(i).getValue2() > deltaTime) {
            assertEquals(deltaTime, resultList.get(i).getValue2());
            assertEquals(testList.get(i).getValue() * resultList.get(i).getValue2() / testList.get(i).getValue2(), resultList.get(i).getValue(), DELTA);
        } else {
            assertEquals(testList.get(i).getValue2(), resultList.get(i).getValue2());
            assertEquals(testList.get(i).getValue(), resultList.get(i).getValue(), DELTA);
        }
        assertEquals(testList.get(i).getTimestamp().getTime(), resultList.get(i).getTimestamp().getTime());
    }
    assertEquals(testList.get(size - 1).getValue2(), resultList.get(size - 1).getValue2());
    assertEquals(testList.get(size - 1).getValue(), resultList.get(size - 1).getValue(), DELTA);
    assertEquals(testList.get(size - 1).getTimestamp().getTime(), resultList.get(size - 1).getTimestamp().getTime());
}

/**
 * Testet, dass adjustBasalTreatments() bei Uebergabe von falschen oder unsortierten Treatments eine IllegalArgumentException wirft.
 */
@Test
public void testExceptions() {
    //not sorted
    List<VaultEntry> testList = new ArrayList<>();
    testList.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(1000000), 2, 30));
    testList.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(0), 2, 30));

    assertThrows(IllegalArgumentException.class, () -> BasalCalculatorTools.adjustBasalTreatments(testList));

    testList.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(2000000), 2, 30));
    assertThrows(IllegalArgumentException.class, () -> BasalCalculatorTools.adjustBasalTreatments(testList));

    //wrong type
    List<VaultEntry> testList2 = new ArrayList<>();
    testList2.add(new VaultEntry(VaultEntryType.BOLUS_NORMAL, new Date(0), 2, 30));

    assertThrows(IllegalArgumentException.class, () -> BasalCalculatorTools.adjustBasalTreatments(testList2));

    testList2.add(new VaultEntry(VaultEntryType.BOLUS_NORMAL, new Date(2000000), 2, 30));
    assertThrows(IllegalArgumentException.class, () -> BasalCalculatorTools.adjustBasalTreatments(testList2));
}
