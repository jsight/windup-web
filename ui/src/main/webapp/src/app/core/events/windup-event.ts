import {MigrationProject, RegisteredApplication, WindupExecution} from "../../generated/windup-services";

export abstract class WindupEvent {
    public static TYPE = 'WindupEvent';

    protected _type: string;
    protected _source: any;

    constructor(type: string, source: any) {
        this._type = type;
        this._source = source;
    }

    public get type(): string {
        return this._type;
    }

    public get source(): any {
        return this._source;
    }

    public isTypeOf(type: any): boolean {
        return this instanceof type; // this.type === type;
    }
}

export class MigrationProjectEvent extends WindupEvent {
    protected _migrationProject: MigrationProject;
    public static TYPE = 'MigrationProjectEvent';

    constructor(project: MigrationProject, source: any) {
        super(MigrationProjectEvent.TYPE, source);
        this._migrationProject = project;
    }

    public get migrationProject() {
        return this._migrationProject;
    }

}


/**
 * Fired when the page starts loading something asynchronously. Basically, any request to the server.
 */
export class LoadingSomethingStartedEvent extends WindupEvent {
    constructor(source?: any) {
        super("LoadingSomethingStartedEvent", source);
    }
}
/**
 * Fired when the page finishes loading something asynchronously, or it fails with an error.
 */
export class LoadingSomethingFinishedEvent extends WindupEvent {
    constructor(protected _response: any, source?: any) {
        super("LoadingSomethingFinishedEvent", source);
    }
    public getResponse(): any { return this._response; }
}
/**
 * Fired if a HTTP request failed.
 */
export class LoadingSomethingFailedEvent extends WindupEvent {
    constructor(protected _response: any, source?: any) {
        super("LoadingSomethingFailedEvent", source);
    }
    public getResponse(): any { return this._response; }
}



export class UpdateMigrationProjectEvent extends MigrationProjectEvent {

}

export class DeleteMigrationProjectEvent extends MigrationProjectEvent {

}

export class ExecutionEvent extends MigrationProjectEvent {
    public static TYPE = 'ExecutionEvent';
    private _execution: WindupExecution;

    constructor(execution: WindupExecution, project: MigrationProject, source: any) {
        super(project, source);
        this._execution = execution;
    }

    get execution(): WindupExecution {
        return this._execution;
    }

    public get type(): string {
        return ExecutionEvent.TYPE;
    }
}

export class NewExecutionStartedEvent extends ExecutionEvent {
    public static TYPE = 'NewExecutionStartedEvent';

    public get type(): string {
        return NewExecutionStartedEvent.TYPE;
    }
}

export class ExecutionUpdatedEvent extends ExecutionEvent {
    public static TYPE = 'ExecutionUpdatedEvent';

    public get type(): string {
        return ExecutionUpdatedEvent.TYPE;
    }
}

export class ExecutionCompletedEvent extends ExecutionUpdatedEvent {
    public static TYPE = 'ExecutionCompletedEvent';

    public get type(): string {
        return ExecutionCompletedEvent.TYPE;
    }
}



export class ApplicationRegistrationEvent extends MigrationProjectEvent {
    public static TYPE = 'ApplicationRegistrationEvent';
    private _applications: RegisteredApplication[];

    constructor(project: MigrationProject, application: RegisteredApplication|RegisteredApplication[], source: any) {
        super(project, source);

        if (!application.hasOwnProperty('length')) {
            this._applications = [ <RegisteredApplication>application ];
        } else {
            this._applications = <RegisteredApplication[]>application;
        }
    }

    public get applications(): RegisteredApplication[] {
        return this._applications;
    }

    public get type(): string {
        return ApplicationRegistrationEvent.TYPE;
    }
}

export class ApplicationRegisteredEvent extends ApplicationRegistrationEvent {

}

export class ApplicationDeletedEvent extends ApplicationRegistrationEvent {

}
