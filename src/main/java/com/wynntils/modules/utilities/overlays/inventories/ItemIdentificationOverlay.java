/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.RainbowText;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.item.ItemGuessProfile;
import com.wynntils.webapi.profiles.item.ItemProfile;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemIdentificationOverlay implements Listener {

    public final static String E = new String(new char[]{(char) 0xB2}), B = new String(new char[]{(char) 0xBD}), L = new String(new char[]{(char) 0xBC});
    private final static Pattern BRACKETS = Pattern.compile("\\[.*?\\]");
    private final static Pattern ID_PERCENTAGES = Pattern.compile("( \\[\\d{1,3}%\\]$)|( (" + TextFormatting.GREEN + "|" + TextFormatting.AQUA + "|" + TextFormatting.RED + ")" + TextFormatting.BOLD + "[\\u21E9\\u21E7\\u21EA]" + TextFormatting.RESET + "(" + TextFormatting.GREEN + "|" + TextFormatting.AQUA + "|" + TextFormatting.RED + ")\\d+\\.\\d+%)|( (" + TextFormatting.DARK_GREEN + "|" + TextFormatting.DARK_RED + ")\\[(" + TextFormatting.GREEN + "|" + TextFormatting.RED + ")[-+]?\\d+(" + TextFormatting.DARK_GREEN + "|" + TextFormatting.DARK_RED + "),(" + TextFormatting.GREEN + "|" + TextFormatting.RED + ") [-+]?\\d+(" + TextFormatting.DARK_GREEN + "|" + TextFormatting.DARK_RED + ")\\])|( (" + TextFormatting.GREEN + "|" + TextFormatting.RED + ")\\[[-+]?\\d+ SP\\])");
    private final static Pattern MARKET_PRICE = Pattern.compile("[-x] " + TextFormatting.WHITE + "([\\d,]+)" + TextFormatting.GRAY + E);
    private final static Pattern SPLIT_MARKET_PRICE = Pattern.compile("\\((\\d+stx)? ?(\\d+" + E + ")? ?(\\d+" + E + B + ")? ?([\\d.]+" + L + E + ")?\\)");
    private final static Pattern STX_PATTERN = Pattern.compile("(\\([^)]*)%stx%([^)]*\\))");
    private final static Pattern LE_PATTERN = Pattern.compile("(\\([^)]*)%le%([^)]*\\))");
    private final static Pattern EB_PATTERN = Pattern.compile("(\\([^)]*)%eb%([^)]*\\))");
    private final static Pattern E_PATTERN = Pattern.compile("(\\([^)]*)%e%([^)]*\\))");
    public static final DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###");

    @SubscribeEvent
    public void onChest(GuiOverlapEvent.ChestOverlap.DrawScreen e) {
        if (e.getGuiInventory().getSlotUnderMouse() != null && e.getGuiInventory().getSlotUnderMouse().getHasStack()) {
            drawHoverGuess(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
            drawHoverItem(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
        }
    }

    @SubscribeEvent
    public void onPlayerInventory(GuiOverlapEvent.InventoryOverlap.DrawScreen e) {
        if (e.getGuiInventory().getSlotUnderMouse() != null && e.getGuiInventory().getSlotUnderMouse().getHasStack()) {
            drawHoverGuess(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
            drawHoverItem(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
        }
    }

    @SubscribeEvent
    public void onHorseInventory(GuiOverlapEvent.HorseOverlap.DrawScreen e) {
        if (e.getGuiInventory().getSlotUnderMouse() != null && e.getGuiInventory().getSlotUnderMouse().getHasStack()) {
            drawHoverGuess(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
            drawHoverItem(e.getGuiInventory().getSlotUnderMouse().getStack(), e.getGuiInventory().getSlotUnderMouse().inventory);
        }
    }

    public void drawHoverGuess(ItemStack stack, IInventory inventory) {
        if (stack.isEmpty() || !stack.hasDisplayName()) {
            return;
        }

        if (stack.getItem() == Items.NETHER_STAR && stack.getDisplayName().contains("Soul Point")) {
            List<String> lore = Utils.getLore(stack);
            if (lore != null && !lore.isEmpty()) {
                if (lore.get(lore.size() - 1).contains("Time until next soul point: ")) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                }
                lore.add("");
                int secondsUntilSoulPoint = PlayerInfo.getPlayerInfo().getTicksToNextSoulPoint() / 20;
                int minutesUntilSoulPoint = secondsUntilSoulPoint / 60;
                secondsUntilSoulPoint %= 60;
                lore.add(TextFormatting.AQUA + "Time until next soul point: " + TextFormatting.WHITE + minutesUntilSoulPoint + ":" + String.format("%02d", secondsUntilSoulPoint));
                NBTTagCompound nbt = stack.getTagCompound();
                NBTTagCompound display = nbt.getCompoundTag("display");
                NBTTagList tag = new NBTTagList();
                lore.forEach(s -> tag.appendTag(new NBTTagString(s)));
                display.setTag("Lore", tag);
                nbt.setTag("display", display);
                stack.setTagCompound(nbt);
                return;
            }
        }

        if (inventory.getName().contains("Marketplace") && !stack.getTagCompound().getBoolean("pricePatternSet") && UtilitiesConfig.Market.INSTANCE.displayInCustomFormat) {
            List<String> lore = Utils.getLore(stack);
            if (lore != null && lore.size() > 2) {
                String price = lore.get(2);
                Matcher priceMatcher = MARKET_PRICE.matcher(price);
                if (priceMatcher.find()) {
                    String actualPriceString = priceMatcher.group(1).replace(",", "");
                    double priceDouble = Double.parseDouble(actualPriceString);
                    
                    int stx = (int) Math.floor(priceDouble / 262144);
                    int le = (int) Math.floor(priceDouble % 262144 / 4096);
                    int eb = (int) Math.floor(priceDouble % 4096 / 64);
                    int e = (int) Math.floor(priceDouble % 64);
                    
                    String formedPriceString = UtilitiesConfig.Market.INSTANCE.customFormat;

                    formedPriceString = STX_PATTERN.matcher(formedPriceString).replaceAll(stx != 0 ? "$1" + stx + "$2" : "");
                    formedPriceString = LE_PATTERN.matcher(formedPriceString).replaceAll(le != 0 ? "$1" + le + "$2" : "");
                    formedPriceString = EB_PATTERN.matcher(formedPriceString).replaceAll(eb != 0 ? "$1" + eb + "$2" : "");
                    formedPriceString = E_PATTERN.matcher(formedPriceString).replaceAll(e != 0 ? "$1" + e + "$2" : "");

                    formedPriceString = formedPriceString
                        .replace("%les%", L + E)
                        .replace("%ebs%", E + B)
                        .replace("%es%", E);

                    formedPriceString = formedPriceString
                        .replace("\\", "\\\\")
                        .replace("$", "\\$")
                        .replace("(", "")
                        .replace(")", "");

                    Matcher splitPriceMatcher = SPLIT_MARKET_PRICE.matcher(price);
                    price = splitPriceMatcher.replaceAll("(" + formedPriceString + ")");
                    stack.getSubCompound("display").getTagList("Lore", 8).set(2, new NBTTagString(price));
                    stack.getTagCompound().setBoolean("pricePatternSet", true);
                }
            }
        }

        if (!stack.getDisplayName().contains("Unidentified")) {
            return;
        }

        String displayWC = TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
        String itemType = displayWC.split(" ")[1];
        String level = null;

        List<String> lore = Utils.getLore(stack);

        for (String aLore : lore) {
            if (aLore.contains("Lv. Range")) {
                level = TextFormatting.getTextWithoutFormattingCodes(aLore).replace("- Lv. Range: ", "");
                break;
            }
        }

        if (itemType == null || level == null) {
            return;
        }

        if (!WebManager.getItemGuesses().containsKey(level)) {
            return;
        }

        ItemGuessProfile igp = WebManager.getItemGuesses().get(level);
        if (igp == null || !igp.getItems().containsKey(itemType)) {
            return;
        }

        String items = null;
        TextFormatting color = null;

        if (stack.getDisplayName().startsWith(TextFormatting.AQUA.toString()) && igp.getItems().get(itemType).containsKey("Legendary")) {
            items = igp.getItems().get(itemType).get("Legendary");
            color = TextFormatting.AQUA;
        } else if (stack.getDisplayName().startsWith(TextFormatting.LIGHT_PURPLE.toString()) && igp.getItems().get(itemType).containsKey("Rare")) {
            items = igp.getItems().get(itemType).get("Rare");
            color = TextFormatting.LIGHT_PURPLE;
        } else if (stack.getDisplayName().startsWith(TextFormatting.YELLOW.toString()) && igp.getItems().get(itemType).containsKey("Unique")) {
            items = igp.getItems().get(itemType).get("Unique");
            color = TextFormatting.YELLOW;
        } else if (stack.getDisplayName().startsWith(TextFormatting.DARK_PURPLE.toString()) && igp.getItems().get(itemType).containsKey("Mythic")) {
            items = igp.getItems().get(itemType).get("Mythic");
            color = TextFormatting.DARK_PURPLE;
        } else if (stack.getDisplayName().startsWith(TextFormatting.RED.toString()) && igp.getItems().get(itemType).containsKey("Fabled")) {
            items = igp.getItems().get(itemType).get("Fabled");
            color = TextFormatting.RED;
        } else if (stack.getDisplayName().startsWith(TextFormatting.GREEN.toString()) && igp.getItems().get(itemType).containsKey("Set")) {
            items = igp.getItems().get(itemType).get("Set");
            color = TextFormatting.GREEN;
        }

        if (items != null) {
            if (lore.get(lore.size() - 1).contains("7Possibilities")) {
                return;
            }
            lore.add(TextFormatting.GREEN + "- " + TextFormatting.GRAY + "Possibilities: " + color + items);

            NBTTagCompound nbt = stack.getTagCompound();
            NBTTagCompound display = nbt.getCompoundTag("display");
            NBTTagList tag = new NBTTagList();

            lore.forEach(s -> tag.appendTag(new NBTTagString(s)));

            display.setTag("Lore", tag);
            nbt.setTag("display", display);
            stack.setTagCompound(nbt);
        }
    }

    public static void drawHoverItem(ItemStack stack, IInventory inventory) {
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("rainbowTitle")) {
            stack.setStackDisplayName(TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName()));
            stack.setStackDisplayName(RainbowText.makeRainbow("Perfect " + stack.getTagCompound().getString("rainbowTitle"), false));
            if (stack.getTagCompound().hasKey("rainbowTitleExtra"))
                stack.setStackDisplayName(stack.getDisplayName() + stack.getTagCompound().getString("rainbowTitleExtra"));
        }
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("verifiedWynntils") && stack.getTagCompound().getBoolean("showChances") == Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && stack.getTagCompound().getBoolean("showRanges") == Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) return;
        boolean showChances = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean showRanges = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        if (!WebManager.getItems().containsKey(Utils.stripExtended(cleanse(stack.getDisplayName()), 1)))
            return;

        ItemProfile wItem = WebManager.getItems().get(Utils.stripExtended(cleanse(stack.getDisplayName()), 1));

        if (wItem.isIdentified()) {
            return;
        }

        int total = 0;
        int identifications = 0;
        double chanceUp = 0;
        double chanceDown = 0;
        int totalSP = 0;
        boolean setBonusStart = false;

        List <String> actualLore = Utils.getLore(stack);
        List <Integer> statOrderMem = new ArrayList<>();

        for (int i = 0; i < actualLore.size(); i++) {
            String lore = cleanse(actualLore.get(i));
            String wColor = TextFormatting.getTextWithoutFormattingCodes(lore);

            if(wColor.matches(".*(Mythic|Fabled|Legendary|Rare|Unique|Set) Item.*") && !lore.contains(E)) {
                int rerollValue = 0;

                //thanks dukiooo for this Math
                if(wColor.contains("Mythic")) {
                    rerollValue = (int)Math.ceil(90.0D + wItem.getLevel() * 18);
                }else if(wColor.contains("Fabled")) {
                    rerollValue = 10; //TODO find the math for rerolling fabled items
                }else if(wColor.contains("Legendary")) {
                    rerollValue = (int)Math.ceil(40D + wItem.getLevel() * 5.2);
                }else if(wColor.contains("Rare")) {
                    rerollValue = (int)Math.ceil(15D + wItem.getLevel() * 1.2d);
                }else if(wColor.contains("Set")) {
                    rerollValue = (int)Math.ceil(12D + wItem.getLevel() * 1.6d);
                }else if(wColor.contains("Unique")) {
                    rerollValue = (int)Math.ceil(5D + wItem.getLevel() * 0.5d);
                }

                int alreadyRolled = 1;
                Matcher m = BRACKETS.matcher(wColor);
                if(m.find()) {
                    alreadyRolled = Integer.parseInt(m.group().replace("[", "").replace("]", ""));
                }

                for(int bb = 1; bb <= alreadyRolled; bb++) rerollValue *= 5;

                actualLore.set(i, lore + TextFormatting.GREEN + " [" + decimalFormat.format(rerollValue) + E + "]");
                break;
            }

            if (lore.contains("Set") && lore.contains("Bonus")) {
                setBonusStart = true;
                continue;
            }

            if (!wColor.startsWith("+") && !wColor.startsWith("-") || setBonusStart) {
                actualLore.set(i, lore);
                continue;
            }

            String[] values = wColor.split(" ");

            if (values.length < 2) {
                actualLore.set(i, lore);
                continue;
            }

            String pField = StringUtils.join(Arrays.copyOfRange(values, 1, values.length), " ").replace("*", "");

            if (pField == null) {
                actualLore.set(i, lore);
                continue;
            }

            boolean raw = !lore.contains("%");

            try {
                int amount = Integer.parseInt(values[0].replace("*", "").replace("%", "").replace("/3s", "").replace("/4s", "").replace("tier ", ""));

                String fieldName;
                if (raw) {
                    fieldName = Utils.getFieldName("raw" + pField);
                    if (fieldName == null) {
                        fieldName = Utils.getFieldName(pField);
                    }
                } else {
                    fieldName = Utils.getFieldName(pField);
                }

                if (fieldName == null) {
                    actualLore.set(i, lore);
                    statOrderMem.add(1000);
                    continue;
                }

                Field f = wItem.getClass().getField(fieldName);
                if (f == null) {
                    actualLore.set(i, lore);
                    continue;
                }

                int itemVal = Integer.parseInt(String.valueOf(f.get(wItem)));
                int min;
                int max;
                if (amount < 0) {
                    max = (int) Math.min(Math.round(itemVal * 1.3d), -1);
                    min = (int) Math.min(Math.round(itemVal * 0.7d), -1);
                } else {
                    max = (int) Math.max(Math.round(itemVal * 1.3d), 1);
                    min = (int) Math.max(Math.round(itemVal * 0.3d), 1);
                }

                if (max == min) {
                    actualLore.set(i, lore);
                } else


                if (showChances) {
                    float downPercent;
                    float upPercent;
                    float bestPercent;
                    if (amount < 0) {
                        downPercent = 0;
                        upPercent = 0;
                        bestPercent = 0;
                        for (double j = 70; j <= 130; j++) {
                            if (Math.round(itemVal * (j / 100)) < amount) {
                                downPercent++;
                            } else if (Math.round(itemVal * (j / 100)) > amount) {
                                upPercent++;
                            }
                            if (Math.round(itemVal * (j / 100)) == min) {
                                bestPercent++;
                            }
                        }
                        downPercent = downPercent / 0.61f;
                        upPercent = upPercent / 0.61f;
                        bestPercent = bestPercent / 0.61f;

                        // Equations for calculating percent chances (not used currently because of a weird offset issue)
                        //downPercent = (amount == max ? 0 : 100 - (float) (((Math.ceil(((amount - 0.5d) / itemVal) * 100) - 70) / 61) * 100));
                        //upPercent = (amount == min ? 0 : (float) (((Math.ceil(((amount + 0.5d) / itemVal) * 100) - 69) / 61) * 100) );
                        //bestPercent = (float) (((Math.ceil(((min * 100d) - 50d) / itemVal) - 70) / 61) * 100);
                    } else {
                        downPercent = 0;
                        upPercent = 0;
                        bestPercent = 0;
                        for (double j = 30; j <= 130; j++) {
                            if (Math.round(itemVal * (j / 100)) < amount) {
                                downPercent++;
                            } else if (Math.round(itemVal * (j / 100)) > amount) {
                                upPercent++;
                            }
                            if (Math.round(itemVal * (j / 100)) == max) {
                                bestPercent++;
                            }
                        }
                        downPercent = downPercent / 1.01f;
                        upPercent = upPercent / 1.01f;
                        bestPercent = bestPercent / 1.01f;

                        // Equations for calculating percent chances (not used currently because of a weird offset issue)
                        //downPercent = (amount == min ? 0 : (float) (((Math.ceil(((amount - 0.5d) / itemVal) * 100) - 30) / 101) * 100) );
                        //upPercent =  (amount == max ? 0 : 100 - (float) (((Math.ceil(((amount + 0.5d) / itemVal) * 100) - 30) / 101) * 100));
                        //bestPercent = 100 - (float) (((Math.ceil(((max - 0.5d) / itemVal) * 100) - 30) / 101) * 100);
                    }

                    lore += " " + TextFormatting.RED.toString() + TextFormatting.BOLD + "\u21E9" + TextFormatting.RESET + TextFormatting.RED + String.format("%.1f", downPercent) + "% " + TextFormatting.GREEN + TextFormatting.BOLD + "\u21E7" + TextFormatting.RESET + TextFormatting.GREEN + String.format("%.1f", upPercent) + "% " + TextFormatting.AQUA + TextFormatting.BOLD + "\u21EA" + TextFormatting.RESET + TextFormatting.AQUA + String.format("%.1f", bestPercent) + "%";
                    identifications += 1;

                    chanceUp = chanceUp + ((1 - chanceUp) * (upPercent / 100));
                    chanceDown = chanceDown + ((1 - chanceDown) * (downPercent / 100));
                } else if (showRanges) {
                    lore += " " + (amount < 0 ? TextFormatting.DARK_RED + "[" + TextFormatting.RED + max + TextFormatting.DARK_RED + "," + TextFormatting.RED + " " + min + TextFormatting.DARK_RED + "]" : TextFormatting.DARK_GREEN + "[" + TextFormatting.GREEN + min + TextFormatting.DARK_GREEN + "," + TextFormatting.GREEN +" " + max + TextFormatting.DARK_GREEN + "]");
                    switch (fieldName) {
                        case "agilityPoints":
                        case "intelligencePoints":
                        case "defensePoints":
                        case "strengthPoints":
                        case "dexterityPoints":
                            totalSP += amount;
                            break;
                        default:
                            break;
                    }
                    identifications++;
                } else {
                    double intVal = max - min;
                    double pVal = amount - min;
                    int percent = (int) ((pVal / intVal) * 100);

                    TextFormatting color;

                    if (amount < 0) percent = 100 - percent;

                    if (percent >= 97) {
                        color = TextFormatting.AQUA;
                    } else if (percent >= 80) {
                        color = TextFormatting.GREEN;
                    } else if (percent >= 30) {
                        color = TextFormatting.YELLOW;
                    } else {
                        color = TextFormatting.RED;
                    }

                    lore += color + " [" + percent + "%]";
                    total += percent;
                    identifications += 1;
                }


                int idRank = Utils.getFieldRank(fieldName);
                if (statOrderMem.isEmpty()){
                    statOrderMem.add(idRank);
                    actualLore.set(i, lore);
                    continue;
                }
                boolean notRepositioned = true;
                for (int j = statOrderMem.size() -1; j >= 0; j--) {
                    if (idRank < statOrderMem.get(j)) {
                        statOrderMem.add(j+1, idRank);
                        actualLore.add(i-(j+1), lore);
                        actualLore.remove(i+1);
                        notRepositioned = false;
                        break;
                    }
                }
                if (notRepositioned) {
                    statOrderMem.add(0, idRank);
                    actualLore.set(i, lore);
                }

            } catch (Exception ex) {
                actualLore.set(i, lore);
            }
        }

        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setBoolean("verifiedWynntils", true);
        nbt.setBoolean("showChances", showChances);
        nbt.setBoolean("showRanges", showRanges);

        if (identifications > 0) {
            int average = total / identifications;
            if (average >= 100) nbt.setString("rainbowTitle", "");
            if (showChances) {

                NBTTagCompound display = nbt.getCompoundTag("display");
                NBTTagList tag = new NBTTagList();

                actualLore.forEach(s -> tag.appendTag(new NBTTagString(s)));

                String name;
                if (nbt.hasKey("rainbowTitle")) {
                    name = Utils.stripPerfect(cleanse(display.getString("Name")));
                } else {
                    name = cleanse(display.getString("Name"));
                }

                display.setTag("Lore", tag);
                if (!nbt.hasKey("rainbowTitle")) {
                    display.setString("Name", name + " " + TextFormatting.RED + TextFormatting.BOLD + "\u21E9" + TextFormatting.RESET + TextFormatting.RED + String.format("%.1f", (chanceDown / (chanceDown + chanceUp)) * 100) + "% " + TextFormatting.GREEN + TextFormatting.BOLD + "\u21E7" + TextFormatting.RESET + TextFormatting.GREEN + String.format("%.1f", (chanceUp / (chanceDown + chanceUp)) * 100) + "%");
                } else {
                    nbt.setString("rainbowTitleExtra", " " + TextFormatting.RED + TextFormatting.BOLD + "\u21E9" + TextFormatting.RESET + TextFormatting.RED + String.format("%.1f", (chanceDown / (chanceDown + chanceUp)) * 100) + "% " + TextFormatting.GREEN + TextFormatting.BOLD + "\u21E7" + TextFormatting.RESET + TextFormatting.GREEN + String.format("%.1f", (chanceUp / (chanceDown + chanceUp)) * 100) + "%");
                }
                nbt.setTag("display", display);
            } else if (showRanges) {
                String Extra = "";

                if (totalSP > 0) {
                    Extra += " " + TextFormatting.GREEN + "[" + totalSP + " SP]";
                } else if (totalSP < 0) {
                    Extra += " " + TextFormatting.RED +"[" + totalSP + " SP]";
                }

                NBTTagCompound display = nbt.getCompoundTag("display");
                NBTTagList tag = new NBTTagList();

                actualLore.forEach(s -> tag.appendTag(new NBTTagString(s)));

                String name;
                if (nbt.hasKey("rainbowTitle")) {
                    name = Utils.stripPerfect(TextFormatting.getTextWithoutFormattingCodes(cleanse(display.getString("Name"))));
                } else {
                    name = cleanse(display.getString("Name"));
                }

                display.setTag("Lore", tag);
                if (!nbt.hasKey("rainbowTitle")) {
                    display.setString("Name", name + Extra);
                } else {
                    nbt.setString("rainbowTitleExtra", Extra);
                }
                nbt.setTag("display", display);
            } else {
                TextFormatting color;
                if (average >= 97) {
                    color = TextFormatting.AQUA;
                } else if (average >= 80) {
                    color = TextFormatting.GREEN;
                } else if (average >= 30) {
                    color = TextFormatting.YELLOW;
                } else {
                    color = TextFormatting.RED;
                }

                NBTTagCompound display = nbt.getCompoundTag("display");
                NBTTagList tag = new NBTTagList();

                actualLore.forEach(s -> tag.appendTag(new NBTTagString(s)));

                String name;
                if (nbt.hasKey("rainbowTitle")) {
                    name = Utils.stripPerfect(TextFormatting.getTextWithoutFormattingCodes(cleanse(display.getString("Name"))));
                } else {
                    name = cleanse(display.getString("Name"));
                }

                display.setTag("Lore", tag);
                display.setString("Name", name + color + " [" + average + "%]");

                if(average >= 100) nbt.setString("rainbowTitle", name);

                nbt.setTag("display", display);
                nbt.setString("rainbowTitleExtra", "");
            }
        }

        stack.setTagCompound(nbt);
    }

    private static String cleanse(String str){
        return ID_PERCENTAGES.matcher(str).replaceAll("");
    }
}
