/**
 * Erstellt Profil mit zufaelligen Basalraten um 00:00 und 00:30 Uhr. Erstellt drei zufaellige Basal Treatments
 * von welchen das erste im ersten Basal Profil, das zweite in beiden, und das dritte nur im zweiten Profil liegen.
 * Es wird getestet, dass die Methode calcBasalDifference() die Basal Treatments und Basal Profile korrekt miteinander verrechnet.
 * Dann wird ueberprueft, dass die Methode korrekt mit Treatments umgeht, die von einem Tag in den naechsten reichen und
 * dass Treatments mit einer Laenge von 0 Minuten entfernt werden.
 */
@Test
public void testCalcTemp() {
    Random random = new Random();
    List<Profile.BasalProfile> basalProfiles = new ArrayList<>();
    double profileRate1 = random.nextDouble();
    double profileRate2 = random.nextDouble();
    basalProfiles.add(new Profile.BasalProfile(LocalTime.of(0, 0), profileRate1));
    basalProfiles.add(new Profile.BasalProfile(LocalTime.of(0, 30), profileRate2));
    Profile profile = new Profile(ZoneId.of("Zulu"), 0, 0, basalProfiles);

    List<VaultEntry> testTreatments = new ArrayList<>();
    testTreatments.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(0), random.nextDouble() * 5, 20));
    testTreatments.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(20 * ONE_MINUTE), random.nextDouble() * 5, 20));
    testTreatments.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(40 * ONE_MINUTE), random.nextDouble() * 5, 20));
    List<VaultEntry> result = BasalCalculatorTools.calcBasalDifference(testTreatments, profile);

    assertEquals(4, result.size());
    VaultEntry entry = testTreatments.get(0);
    VaultEntry resBasal = result.get(0);
    assertEquals(entry.getValue2(), resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime(), resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate1 / 60, resBasal.getValue(), DELTA);

    entry = testTreatments.get(1);
    resBasal = result.get(1);
    assertEquals(entry.getValue2() / 2, resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime(), resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate1 / 60, resBasal.getValue(), DELTA);

    resBasal = result.get(2);
    assertEquals(entry.getValue2() / 2, resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime() + 10 * ONE_MINUTE, resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate2 / 60, resBasal.getValue(), DELTA);

    entry = testTreatments.get(2);
    resBasal = result.get(3);
    assertEquals(entry.getValue2(), resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime(), resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate2 / 60, resBasal.getValue(), DELTA);

    testTreatments = new ArrayList<>();
    testTreatments.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date(), 0, 0));
    result = BasalCalculatorTools.calcBasalDifference(testTreatments, profile);
    assertEquals(0, result.size());

    testTreatments = new ArrayList<>();
    testTreatments.add(new VaultEntry(VaultEntryType.BASAL_MANUAL, new Date((24 * 60 * ONE_MINUTE) - (10 * ONE_MINUTE)), random.nextDouble(), 20));
    result = BasalCalculatorTools.calcBasalDifference(testTreatments, profile);
    assertEquals(2, result.size());
    entry = testTreatments.get(0);
    resBasal = result.get(0);
    assertEquals(entry.getValue2() / 2, resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime(), resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate2 / 60, resBasal.getValue(), DELTA);

    resBasal = result.get(1);
    assertEquals(entry.getValue2() / 2, resBasal.getValue2());
    assertEquals(entry.getTimestamp().getTime() + 10 * ONE_MINUTE, resBasal.getTimestamp().getTime());
    assertEquals(entry.getValue() / 20 - profileRate1 / 60, resBasal.getValue(), DELTA);
}