/*
 * Magic plugin inspired by Merlin.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.hoot215.merlin.spells;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import me.hoot215.merlin.MagicSpell;
import me.hoot215.merlin.Sorcerer;
import me.hoot215.merlin.Spell;
import me.hoot215.merlin.SpellHandler;
import me.hoot215.merlin.Trigger;

@SpellHandler(name = "Enchant Leather Protection",
    description = "Enchants a leather item with Protection",
    permission = "merlin.cast.enchantment.leatherprotection")
public class EnchantLeatherProtection implements MagicSpell
  {
    private final Set<Material> ARMOUR = new HashSet<Material>();
      
      {
        Collections.addAll(ARMOUR, new Material[] {Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS});
      }
    
    public boolean cast (Sorcerer sorcerer, Trigger trigger, Event event)
      {
        ItemStack hand = sorcerer.getPlayer().getItemInHand();
        if (isArmour(hand))
          {
            int level =
                (int) Math.ceil(sorcerer.getMagicks().getLevel(
                    Spell.Type.ENCHANTMENT) / 5.0);
            if (level > 4)
              {
                level = 4;
              }
            Map<Enchantment, Integer> enchantments =
                new HashMap<Enchantment, Integer>();
            enchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            enchantments.put(Enchantment.PROTECTION_EXPLOSIONS, level);
            enchantments.put(Enchantment.PROTECTION_FALL, level);
            enchantments.put(Enchantment.PROTECTION_FIRE, level);
            enchantments.put(Enchantment.PROTECTION_PROJECTILE, level);
            hand.addEnchantments(enchantments);
            return true;
          }
        else
          {
            sorcerer.getPlayer()
                .sendMessage(
                    ChatColor.RED
                        + "That item cannot be enchanted with this spell");
            return false;
          }
      }
    
    private boolean isArmour (ItemStack item)
      {
        return ARMOUR.contains(item.getType());
      }
  }
