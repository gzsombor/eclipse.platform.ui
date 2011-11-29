/*******************************************************************************
 * Copyright (c) 2010-2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.addons;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Parameter;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Process commands in the model, feeding them into the command service.
 */
public class CommandProcessingAddon {
	@Inject
	private ECommandService commandService;

	@Inject
	private CommandManager commandManager;

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	private Category undefinedCategory;

	private EventHandler additionHandler;

	@PostConstruct
	public void init() {
		Activator.trace(Policy.DEBUG_CMDS, "Initialize commands from model", null); //$NON-NLS-1$
		undefinedCategory = commandService.defineCategory(MApplication.class.getName(),
				"Application Category", null); //$NON-NLS-1$
		createCategories();
		createCommands();
		registerModelListeners();
	}

	@PreDestroy
	public void dispose() {
		unregsiterModelListeners();
	}

	private void registerModelListeners() {
		additionHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if (application == event.getProperty(UIEvents.EventTags.ELEMENT)) {
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
						Object obj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
						if (obj instanceof MCommand) {
							createCommand((MCommand) obj);
						} else if (obj instanceof MCategory) {
							createCategory((MCategory) obj);
						}
					}
				}
			}
		};
		broker.subscribe(UIEvents.Application.TOPIC_COMMANDS, additionHandler);
		broker.subscribe(UIEvents.Application.TOPIC_CATEGORIES, additionHandler);
	}

	private void unregsiterModelListeners() {
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
	}

	private void createCommands() {
		for (MCommand cmdModel : application.getCommands()) {
			createCommand(cmdModel);
		}
	}

	private void createCommand(MCommand cmdModel) {
		IParameter[] parms = null;
		String id = cmdModel.getElementId();
		String name = localize(cmdModel.getCommandName(), cmdModel);
		String desc = localize(cmdModel.getDescription(), cmdModel);
		List<MCommandParameter> modelParms = cmdModel.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			ArrayList<Parameter> parmList = new ArrayList<Parameter>();
			for (MCommandParameter cmdParm : modelParms) {
				ParameterType parameterType = null;
				if (cmdParm.getTypeId() != null && cmdParm.getTypeId().length() > 0) {
					parameterType = commandManager.getParameterType(cmdParm.getTypeId());
				}
				parmList.add(new Parameter(cmdParm.getElementId(), cmdParm.getName(), null,
						parameterType, cmdParm.isOptional()));
			}
			parms = parmList.toArray(new Parameter[parmList.size()]);
		}
		Category cat = undefinedCategory;
		if (cmdModel.getCategory() != null) {
			cat = commandService.getCategory(cmdModel.getCategory().getElementId());
		}
		commandService.defineCommand(id, name, desc, cat, parms);
	}

	private void createCategories() {
		for (MCategory catModel : application.getCategories()) {
			createCategory(catModel);
		}
	}

	private void createCategory(MCategory catModel) {
		Category category = commandService.getCategory(catModel.getElementId());
		if (!category.isDefined()) {
			category.define(localize(catModel.getName(), catModel),
					localize(catModel.getDescription(), catModel));
		}
	}

	/**
	 * Attempt to localize the provided key. Return the localized variant if found or the key itself
	 * otherwise.
	 * 
	 * @param key
	 *            the possible key reference
	 * @param modelElement
	 *            the defining model element
	 * @return the localized variant if found, or the key
	 */
	private String localize(String key, MApplicationElement modelElement) {
		if (key == null) {
			return null;
		}
		String localized = LocalizationHelper.getLocalized(key, modelElement);
		return localized == null ? key : localized;
	}
}
