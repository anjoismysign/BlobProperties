package io.github.anjoismysign.blobproperties.command;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyManager;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalParty;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InternalPropertyType;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.CommandTargetBuilder;
import io.github.anjoismysign.skeramidcommands.commandtarget.LogicCommandParameters;
import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum BlobPropertiesCommand {
    INSTANCE;

    private static final String COMMAND_NAME = "properties";
    private static final String COMMAND_PERMISSION = "properties.command";
    private static final String COMMAND_DESCRIPTION = "Base command for BlobProperties plugin";

    private static final Command COMMAND = BukkitAdapter.getInstance().createCommand(COMMAND_NAME, COMMAND_PERMISSION, COMMAND_DESCRIPTION);

    private static final BlobLibMessageAPI MESSAGE_API = BlobLibMessageAPI.getInstance();

    public void setup(@NotNull PropertiesManagerDirector director) {
        Command home = COMMAND.child("home"); //home's permission is "properties.command.home"
        home.onExecute((permissionMessenger, strings) -> {
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            director.getListenerManager().getPublicPropertyHome().open(player);
        });

        Command partyCommand = COMMAND.child("party");
        Command partyLeave = partyCommand.child("leave");
        Command partyDisband = partyCommand.child("disband");
        CommandTarget<Player> onlinePlayers = BukkitCommandTarget.ONLINE_PLAYERS();
        Command partyInvite = partyCommand.child("invite");
        Command partyVisit = partyCommand.child("visit");
        Command partyKick = partyCommand.child("kick");

        partyLeave.onExecute(((permissionMessenger, args) -> {
            @Nullable Player player = player(permissionMessenger);
            if (player == null) {
                return;
            }
            SerializableProprietor senderProprietor = BlobProperties.getInstance().getProprietor(player);
            if (senderProprietor == null){
                return;
            }
            InternalParty party = (InternalParty) senderProprietor.getCurrentlyAttending();
            if (party == null) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                return;
            }
            if (party.getOwner().getAddress().equals(senderProprietor.getAddress())) {
                MESSAGE_API.getMessage("BlobProprietor.Party-Leader-Cannot-Leave", player).handle(player);
                return;
            }
            party.depart(senderProprietor, false);
        }));

        partyDisband.onExecute(((permissionMessenger, args) -> {
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            SerializableProprietor host = BlobProperties.getInstance().getProprietor(player);
            if (host == null){
                return;
            }
            InternalParty party = (InternalParty) host.getCurrentlyAttending();
            if (party == null) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                return;
            }
            if (!party.getOwnerName().equals(player.getName())) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Party-Leader", player).handle(player);
                return;
            }
            party.disband();
        }));

        partyInvite.setParameters(onlinePlayers);
        partyInvite.onExecute(((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            Player target = onlinePlayers.parse(args[0]);
            if (target == null) {
                MESSAGE_API.getMessage("Player.Not-Found", player).handle(player);
                return;
            }
            SerializableProprietor host = BlobProperties.getInstance().getProprietor(player);
            if (host == null){
                return;
            }
            SerializableProprietor guest = BlobProperties.getInstance().getProprietor(target);
            if (guest == null){
                return;
            }
            InternalParty party = (InternalParty) host.getCurrentlyAttending();
            Property currentlyAt = host.getCurrentlyAt();
            if (currentlyAt == null) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Inside-Property", player).handle(player);
                return;
            }
            if (!host.ownsProperty(currentlyAt)) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Inside-Own-Property", player).handle(player);
                return;
            }
            if (party == null)
                party = new InternalParty(host, currentlyAt);
            MESSAGE_API.getMessage("BlobProprietor.Invited", player)
                    .modify(s -> s.replace("%player%", target.getName()))
                    .handle(player);
            MESSAGE_API.getMessage("BlobProprietor.Received-Invite", target)
                    .modify(s -> s.replace("%player%", player.getName()))
                    .onClick(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/properties party visit " + player.getName()))
                    .handle(target);
            guest.addPendingInvite(host, party);
        }));

        partyVisit.setParameters(onlinePlayers);
        partyVisit.onExecute(((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            Player target = onlinePlayers.parse(args[0]);
            if (target == null) {
                MESSAGE_API.getMessage("Player.Not-Found", player).handle(player);
                return;
            }
            SerializableProprietor guest = BlobProperties.getInstance().getProprietor(player);
            if (guest == null){
                return;
            }
            if (!guest.hasPendingInvite(target)) {
                MESSAGE_API.getMessage("BlobProprietor.No-Pending-Invite", player).handle(player);
                return;
            }
            SerializableProprietor host = BlobProperties.getInstance().getProprietor(target);
            if (host == null){
                return;
            }
            InternalParty party = (InternalParty) host.getCurrentlyAttending();
            if (party == null) {
                MESSAGE_API.getMessage("BlobProprietor.Other-Not-Attending-Party", player)
                        .modify(s -> s.replace("%player%", target.getName()))
                        .handle(player);
                return;
            }
            InternalParty oldParty = (InternalParty) guest.getCurrentlyAttending();
            if (oldParty != null) {
                oldParty.stepOut(guest, false);
                oldParty.unallow(guest);
            }
            if (!party.lodge(guest) && guest.getCurrentlyAt() != null) {
                guest.stepOut(null);
            }
            guest.removePendingInvite(host);
        }));

        partyKick.setParameters(onlinePlayers);
        partyKick.onExecute(((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            Player target = onlinePlayers.parse(args[0]);
            if (target == null) {
                MESSAGE_API.getMessage("Player.Not-Found", player).handle(player);
                return;
            }
            SerializableProprietor host = BlobProperties.getInstance().getProprietor(player);
            if (host == null){
                return;
            }
            SerializableProprietor guest = BlobProperties.getInstance().getProprietor(target);
            if (guest == null){
                return;
            }
            InternalParty party = (InternalParty) host.getCurrentlyAttending();
            if (party == null) {
                MESSAGE_API.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                return;
            }
            Party attending = guest.getCurrentlyAttending();
            Property at = guest.getCurrentlyAt();
            if (attending != null && attending.equals(party)
                    && at != null &&
                    at.identifier().equals(
                            party.getProperty().identifier())) {
                party.stepOut(guest, true);
            }
            guest.removePendingInvite(host);
            party.unallow(guest);
        }));

        Command adminCommand = COMMAND.child("admin");
        Command doorManager = adminCommand.child("doormanager");
        Command containerManager = adminCommand.child("containermanager");
        Command generate = adminCommand.child("generate");

        CommandTarget<Property> propertyCommandTarget = CommandTargetBuilder.fromMap(() -> {
            BlobPropertiesAPI api = BlobPropertiesAPI.getInstance();
            PropertyManager manager = api.getPropertyManager();
            Map<String, Property> map = new HashMap<>();
            for (InternalPropertyType type : InternalPropertyType.values()) {
                String prefix = type.typeName() + "-";
                manager.getPropertiesByMeta(type).forEach(property -> {
                    String identifier = property.identifier();
                    String key = prefix + identifier;
                    map.put(key, property);
                });
            }
            return map;
        });
        doorManager.setParameters(propertyCommandTarget);
        doorManager.onExecute(((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            String key = args[0];
            Property property = propertyCommandTarget.parse(key);
            if (property == null) {
                player.sendMessage(key + " doesn't exist");
                return;
            }
            InternalProperty internalProperty = (InternalProperty) property;
            player.getInventory().addItem(director.getItemStackManager().getDoorManager(internalProperty));
        }));
        CommandTarget<Integer> rowsCommandTarget = LogicCommandParameters.POSITIVE_INTEGER();
        containerManager.setParameters(propertyCommandTarget, rowsCommandTarget);
        containerManager.onExecute(((permissionMessenger, args) -> {
            if (args.length < 2){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            String propertyKey = args[0];
            Property property = propertyCommandTarget.parse(propertyKey);
            if (property == null) {
                player.sendMessage(propertyKey + " doesn't exist");
                return;
            }
            InternalProperty internalProperty = (InternalProperty) property;
            String plainTextRows = args[1];
            Integer rows = rowsCommandTarget.parse(plainTextRows);
            if (rows == null || rows > 6) {
                player.sendMessage("Rows must be between 1 and 6");
                return;
            }
            player.getInventory().addItem(director.getItemStackManager().getContainerManager(internalProperty, rows));
        }));
        CommandTarget<InternalPropertyType> typeTarget = CommandTargetBuilder.fromMap(() -> Arrays.stream(InternalPropertyType.values())
                .map(type -> new AbstractMap.SimpleEntry<>(type.typeName(), type))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )));
        CommandTarget<String> nameTarget = new CommandTarget<>() {
            @Override
            public List<String> get() {
                return List.of("Type the new propery name");
            }

            @Override
            public @Nullable String parse(String s) {
                return s;
            }
        };
        generate.setParameters(typeTarget, nameTarget);
        generate.onExecute(((permissionMessenger, args) -> {
            if (args.length < 2){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null)
                return;
            String typeKey = args[0];
            String identifier = args[1];
            InternalPropertyType type = typeTarget.parse(typeKey);
            if (type == null){
                player.sendMessage("Invalid InternalPropertyType: "+typeKey);
                return;
            }
            String positionableIdentifier = identifier+"_outside";
            BlobLibTranslatableAPI translatableAPI = BlobLibTranslatableAPI.getInstance();
            @Nullable TranslatablePositionable translatablePositionable = translatableAPI.getTranslatablePositionable(positionableIdentifier);
            if (translatablePositionable == null){
                player.sendMessage("You first need to create a TranslatablePositionable by key '"+positionableIdentifier+"'");
                return;
            }
            InternalProperty internalProperty = type.getCreateFunction().apply(identifier);
            internalProperty.save();
            player.sendMessage(typeKey+" '"+identifier+"' was successfully created");
        }));
    }

    @Nullable
    private Player player(@NotNull PermissionMessenger permissionMessenger) {
        CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
        if (!(sender instanceof Player player)) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("System.Console-Not-Allowed-Command", sender)
                    .toCommandSender(sender);
            return null;
        }
        return player;
    }
}
